package com.pc.rocketmqclient.consumer;

import com.pc.rocketmqclient.model.MQConstants;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 保证消息可靠传递
 *
 * @author pengchao
 * @date 17:19 2020-05-31
 */
@Component
public class SafeConsumer implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(NormalConsumer.class);


    /**
     * 初始化消费者，并订阅相关信息
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("safe consumer init.");
        startConsumer("safe_consumer_group_a");
        logger.info("safe consumer started.");
    }


    public void startConsumer(String consumerName) throws MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("safe_consumer_group");
        String namesrvAddr = "localhost:9876";
        if (StringUtils.isEmpty(namesrvAddr)) {
            logger.error("namesrvAddr is empty.");
            return;
        }
        consumer.setNamesrvAddr(namesrvAddr);

        //如果是同个分组下有多个消费者，那么同一个jvm下需要给不同的消费者加实例名
        consumer.setInstanceName(consumerName);

        consumer.subscribe(MQConstants.SAFE_TOPIC, "*");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);


        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);

//                int times = msg.getReconsumeTimes();
//                System.out.println("重试次数："+times);

                try {
                    String body = new String(msg.getBody());

                    System.out.println(consumerName+":topic:" + msg.getTopic() + " ,msgId:" + msg.getMsgId() + " ,mgsBody:" + body);

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    logger.error("Consumer rocketmq error", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });


        try {
            consumer.start();
        } catch (MQClientException e) {
            logger.error("consumer start error:[{}]", e);
        }


    }
}

