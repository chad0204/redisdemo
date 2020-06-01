package com.pc.rocketmqclient.consumer;

import com.pc.rocketmqclient.model.MQConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * @author pengchao
 * @date 10:51 2020-05-30
 */
@Component
public class TransactionConsumer implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(TransactionConsumer.class);


    /**
     * 初始化消费者，并订阅相关信息
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * consumer不能比queue多，多出来的将不能分配到消息，一个queue只能被一个consumer消费，不管什么负载均衡策略，除非广播模式下。
         */
        logger.info("transaction consumer init.");
        startConsumer("transaction_consumer_group_a");
        logger.info("transaction consumer started.");
    }


    public void startConsumer(String consumerName) throws MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("transaction_consumer_group");
        String namesrvAddr = "localhost:9876";
        if (StringUtils.isEmpty(namesrvAddr)) {
            logger.error("namesrvAddr is empty.");
            return;
        }
        consumer.setNamesrvAddr(namesrvAddr);

        //如果是同个分组下有多个消费者，那么同一个jvm下需要给不同的消费者加实例名
        consumer.setInstanceName(consumerName);

        consumer.subscribe(MQConstants.TRANSACTION_TOPIC, "*");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            //不管是批量发送还是单个消息，都会转成list,所以这里get(0)即可
            MessageExt msg = msgs.get(0);
            try {
                String body = new String(msg.getBody());
                System.out.println(consumerName+":topic:" + msg.getTopic() + " ,msgId:" + msg.getMsgId() + " ,mgsBody:" + body);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                logger.error("Consumer rocketmq error", e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });


        try {
            consumer.start();
        } catch (MQClientException e) {
            logger.error("consumer start error:[{}]", e);
        }


    }
}
