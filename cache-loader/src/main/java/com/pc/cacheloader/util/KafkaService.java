package com.pc.cacheloader.util;

import com.alibaba.fastjson.JSON;
import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.cache.DepositTask;
import com.pc.cacheloader.constants.SinkModel;
import com.pc.cacheloader.constants.TVMsg;
import com.pc.cacheloader.loader.AbstractLoader;
import com.pc.cacheloader.model.BaseDO;
import com.pc.cacheloader.model.CacheMsg;
import com.pc.cacheloader.runner.AsyncChannelProcess;
import com.pc.cacheloader.runner.LoaderRunner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class KafkaService {

    @Autowired
    @Qualifier("kafkaProducer")
    private KafkaTemplate<String, String> cacheKafkaProducer;

    @Value("${core.retry.notify.topic}")
    private String retryTopic;

    @Setter
    @Getter
    private ApplicationContext applicationContext;

    @Resource
    @Qualifier("asyncChannelProcess")
    private AsyncChannelProcess<TVMsg> asyncChannelProcess;

    /**
     * 接收外部更新
     *
     * @param cr
     */
    @KafkaListener(topicPattern = "${core.integration.notify.topic}", containerFactory =
            "kafkaNormalListenerContainerFactory")
    private void normalListenEvent(ConsumerRecord<String, String> cr) {
        try {
            CacheMsg cacheMsg = JSON.parseObject(cr.value(), CacheMsg.class);
            if (cacheMsg == null) {
                log.error("cache msg is null,origin:{}", JSON.toJSONString(cr));
            }
            consumer(cacheMsg);
        } catch (Exception e) {
            log.error("[listen] normalListenEvent handle error", e);
        }

    }

    /**
     * 发布重试
     *
     * @param cr
     */
    @KafkaListener(topicPattern = "${core.retry.notify.topic}", containerFactory =
            "kafkaInnerListenerContainerFactory")
    private void innerListenEvent(ConsumerRecord<String, String> cr, Acknowledgment ack) {
        try {
            CacheMsg cacheMsg = JSON.parseObject(cr.value(), CacheMsg.class);
            if (cacheMsg == null) {
                log.error("cache msg is null,origin:{}", JSON.toJSONString(cr));
            }
            syncConsumer(cacheMsg);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("[listen] innerListenEvent handle error", e);
            //超过30s钟，抛弃策略
            if (System.currentTimeMillis() - cr.timestamp() > 30 * 1000) {
                ack.acknowledge();
                log.error("throw old message in case block {}", JSON.toJSONString(cr));
            }
        }


    }

    public void sendToRetry(String msg) {
        cacheKafkaProducer.send(new ProducerRecord("CACHE_RETRY_TOPIC", msg));
    }

    public void sendToSouthNotify(String msg) {
        cacheKafkaProducer.send(new ProducerRecord("CACHE_S_NOTIFY_TOPIC_NULL", msg));
    }

    public String getIdsTopic(String... parts) {
        StringBuffer sb = new StringBuffer("IDS");
        for (int i = 0; i < parts.length; i++) {
            sb.append("_").append(parts[i]);
        }
        return sb.toString();
    }

    /**
     * 将外部任务转换为loader的内部队列，同步
     *
     * @param cacheMsg
     */
    public void consumer(CacheMsg cacheMsg) {
        Class cls = cacheMsg.getType().getaClass();
        if (cls == null) {
            throw new IllegalArgumentException("un support consumer type");
        }
        TVMsg tvMsg = new TVMsg(SinkModel.LOAD, JSON.parseObject(cacheMsg.getValue(), cls), false);
        asyncChannelProcess.add(tvMsg, ((BaseDO) tvMsg.getT()).getChannel());
    }

    /**
     * 将外部任务转换为loader的内部队列，异步
     *
     * @param cacheMsg
     */
    public void syncConsumer(CacheMsg cacheMsg) {
        Class cls = cacheMsg.getType().getaClass();
        if (cls != null) {
            throw new IllegalArgumentException("un support sync consumer type");
        }
        DepositTask depositTask = JSON.parseObject(cacheMsg.getValue(), DepositTask.class);
        AbstractLoader abstractLoader = (AbstractLoader) applicationContext.getBean(depositTask.getLoader());
        CacheTask cacheTask = deposit(depositTask, abstractLoader);
        TVMsg tvMsg = new TVMsg(SinkModel.LOAD, cacheTask, true);//同步重试
        abstractLoader.consumerMsg(tvMsg);
    }

    private CacheTask deposit(DepositTask depositTask, AbstractLoader abstractLoader) {
        return CacheTask.newBuilder()
                .loader(abstractLoader)
                .origin(depositTask.getOrigin())
                .history(depositTask.getHistory())
                .source(1)
                .transfer(depositTask.getTransfer())
                .actionType(depositTask.getActionType())
                .executeStep(depositTask.getExecuteStep())//设置步数
                .initTime(depositTask.getInitTime()).build();
    }

}
