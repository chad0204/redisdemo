package com.pc.rocketmqclient.consumer;

import com.pc.rocketmqclient.model.MQConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
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
 * 1.集群模式下，同一个group下的消费者,一个queue只能被一个consumer消费，但是一个consumer可以消费多个queue。也就是4个q,超过4个consumer，多出来的consumer将不会收到消息。
 * 2.consumer负载均衡策略决定了queue的分配，但是广播模式下，没有负载均衡。
 * 3.一个jvm下，同一个group启动多个消费者需要给每个消费者加实例名称（生产者也是）。
 * 4.广播==不同group的集群
 * 5.三种条件下，即使设置了从最后的offset消费，也会变成从0消费。
 * 6.接收的都是List<MessageExt>，如果不是批量发送，那么get(0)即可
 *
 * @author pengchao
 * @date 10:51 2020-05-30
 */
@Component
public class NormalConsumer implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(NormalConsumer.class);

    private static String GROUP_NAME = "normal_consumer_group";


    /**
     * 初始化消费者，并订阅相关信息
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * consumer不能比queue多，多出来的将不能分配到消息，一个queue只能被一个consumer消费，不管什么负载均衡策略，除非广播模式下。
         */
        logger.info("normal consumer init.");
        startConsumer("normal_consumer_group_a");
        startConsumer("normal_consumer_group_b");
        startConsumer("normal_consumer_group_c");
        startConsumer("normal_consumer_group_d");
        startConsumer("normal_consumer_group_e");//这个consumer将分配不到消息
        logger.info("normal consumer started.");
    }


    public void startConsumer(String consumerName) throws MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(GROUP_NAME);//不同groupName的消费者相当于广播
        String namesrvAddr = "localhost:9876";
        if (StringUtils.isEmpty(namesrvAddr)) {
            logger.error("namesrvAddr is empty.");
            return;
        }
        consumer.setNamesrvAddr(namesrvAddr);

        //如果是同个分组下有多个消费者，那么同一个jvm下需要给不同的消费者加实例名
        consumer.setInstanceName(consumerName);

        consumer.subscribe(MQConstants.NORMAL_TOPIC, "*");
//        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);//跳过历史消息
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

//        consumer.setMessageModel(MessageModel.BROADCASTING);//默认集群

//        consumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueAveragely());//负载均衡策略，广播模式下无效

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
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
            }
        });


        try {
            consumer.start();
        } catch (MQClientException e) {
            logger.error("consumer start error:[{}]", e);
        }


        System.out.println();

        consumer.fetchSubscribeMessageQueues(MQConstants.NORMAL_TOPIC);


    }
}
