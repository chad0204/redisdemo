package com.pc.rocketmqclient.consumer;

import com.pc.rocketmqclient.model.MQConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class NormalPullConsumer implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(NormalPullConsumer.class);

    private static final Map<MessageQueue, Long> OFFSE_TABLE = new HashMap<MessageQueue, Long>();

    private static String GROUP_NAME = "normal_consumer_group";



    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("normal consumer init.");
        startConsumer("pull_consumer_group_a");
        logger.info("normal consumer started.");
    }


    public void startConsumer(String consumerName) throws MQClientException {

        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("please_rename_unique_group_name_5");
        consumer.setNamesrvAddr("127.0.0.1:9876");
        consumer.start();

        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("broker-a");
        for (MessageQueue mq : mqs) {
            System.out.printf("Consume from the queue: %s%n", mq);
            SINGLE_MQ:
            while (true) {
                try {
                    PullResult pullResult =
                            consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 32);
                    System.out.printf("%s%n", pullResult);
                    putMessageQueueOffset(mq, pullResult.getNextBeginOffset());//消费端自己保存offset
                    switch (pullResult.getPullStatus()) {
                        case FOUND:
                            break;
                        case NO_MATCHED_MSG:
                            break;
                        case NO_NEW_MSG:
                            break SINGLE_MQ;
                        case OFFSET_ILLEGAL:
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private static long getMessageQueueOffset(MessageQueue mq) {
        Long offset = OFFSE_TABLE.get(mq);
        if (offset != null)
            return offset;

        return 0;
    }

    private static void putMessageQueueOffset(MessageQueue mq, long offset) {
        OFFSE_TABLE.put(mq, offset);
    }
}
