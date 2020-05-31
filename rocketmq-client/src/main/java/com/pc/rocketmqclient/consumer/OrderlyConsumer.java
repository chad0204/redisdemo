package com.pc.rocketmqclient.consumer;

import com.pc.rocketmqclient.model.MQConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 *
 * @author pengchao
 * @date 10:51 2020-05-30
 */
@Component
public class OrderlyConsumer implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(OrderlyConsumer.class);


    /**
     * 初始化消费者，并订阅相关信息
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("orderly consumer init.");
        startConsumer("orderly_consumer_group_a");
        startConsumer("orderly_consumer_group_b");
        startConsumer("orderly_consumer_group_c");
        startConsumer("orderly_consumer_group_d");
        logger.info("orderly consumer started.");
    }


    public void startConsumer(String consumerName) throws MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("orderly_consumer_group");
        String namesrvAddr = "localhost:9876";
        if (StringUtils.isEmpty(namesrvAddr)) {
            logger.error("namesrvAddr is empty.");
            return;
        }
        consumer.setNamesrvAddr(namesrvAddr);

        //如果是同个分组下有多个消费者，那么同一个jvm下需要给不同的消费者加实例名
        consumer.setInstanceName(consumerName);

        //订阅顺序信息
        consumer.subscribe(MQConstants.ORDERLY_TOPIC, MQConstants.ORDERLY_TAG_1);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);//跳过历史消息

//        consumer.setMessageModel(MessageModel.BROADCASTING);//默认集群

        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                try {
                    MessageExt messageExt = msgs.get(0);
                        String body = new String(messageExt.getBody());
                        System.out.println(consumerName+":topic:" + messageExt.getTopic() + " ,msgId:" + messageExt.getMsgId() + " ,mgsBody:" + body);
                    return ConsumeOrderlyStatus.SUCCESS;
                } catch (Exception e) {
                    logger.error("Consumer rocketmq error", e);
                    return null;
                }
            }
        });


        try {
            consumer.start();
        } catch (MQClientException e) {
            logger.error("consumer start error:[{}]", e);
            return;
        }

    }
}
