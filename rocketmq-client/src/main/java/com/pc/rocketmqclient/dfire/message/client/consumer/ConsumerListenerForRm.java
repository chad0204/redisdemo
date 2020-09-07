package com.pc.rocketmqclient.dfire.message.client.consumer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.pc.rocketmqclient.dfire.message.client.consumer.support.IConsumerHandle;
import com.pc.rocketmqclient.dfire.message.client.to.AsyncMsgRM;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.annotation.PreDestroy;

import static com.pc.rocketmqclient.dfire.message.client.util.MD5Util.stringIsEmpty;

/**
 * 用于 RocketMQ
 *
 * @author pengchao
 * @date 15:05 2020-09-04
 */
public class ConsumerListenerForRm {


    private static Logger logger = LoggerFactory.getLogger(ConsumerListenerForRm.class);
    private DefaultMQPushConsumer consumer;                 // 消费者


    public static <P> Builder<P> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private String consumerGroup;                           // 改消费者所属的组
        private String namesrvAddr;                             // RocketMQ 的 NameSRV
        private MessageModel messageModel = MessageModel.CLUSTERING; // 消费模式。默认是集群模式
        //一个新的订阅组默认第一次启动从队列的最后位置开始消费
        private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
        //回溯消费时间
        private String consumeTimestamp;
        //是否启动顺序消费组
        private RegisterConsumeType registerConsumeType = RegisterConsumeType.CONCURRENTLY;
        //消费消息线程，最小数目
        private int consumeThreadMin = 20;


        private Properties consumerProperties;
        /**
         * topic
         */
        private String topic;

        private IConsumerHandle consumerHandle;

        private Builder() {
        }


        public Builder setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
            return this;
        }

        public Builder setNamesrvAddr(String namesrvAddr) {
            this.namesrvAddr = namesrvAddr;
            return this;
        }

        public Builder setMessageModel(MessageModel messageModel) {
            this.messageModel = messageModel;
            return this;
        }

        public Builder setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
            this.consumeFromWhere = consumeFromWhere;
            return this;
        }

        public Builder setConsumeTimestamp(String consumeTimestamp) {
            this.consumeTimestamp = consumeTimestamp;
            return this;
        }

        public Builder setRegisterConsumeType(RegisterConsumeType registerConsumeType) {
            this.registerConsumeType = registerConsumeType;
            return this;
        }

        public Builder setConsumeThreadMin(int consumeThreadMin) {
            this.consumeThreadMin = consumeThreadMin;
            return this;
        }

        public Builder setConsumerProperties(Properties consumerProperties) {
            this.consumerProperties = consumerProperties;
            return this;
        }

        public Builder setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder setConsumerHandle(IConsumerHandle consumerHandle) {
            this.consumerHandle = consumerHandle;
            return this;
        }

        public ConsumerListenerForRm build() throws MQClientException {

            ConsumerListenerForRm consumerListenerForRm = new ConsumerListenerForRm();
            /**
             * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
             * 注意：ConsumerGroupName需要由应用来保证唯一
             */
            Assert.isTrue(consumerGroup.startsWith("c_") && consumerGroup.contains(topic),
                    "消费者不符合规范！consumerGroup:" + consumerGroup + ",topic:" + topic);

            //广播模式采用动态消费组的方式
            if (messageModel.equals(MessageModel.BROADCASTING)) {
                try {
                    consumerGroup = consumerGroup + InetAddress.getLocalHost().getHostAddress().replace(".", "_");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

            consumerListenerForRm.consumer = new DefaultMQPushConsumer(consumerGroup);
            consumerListenerForRm.consumer.setNamesrvAddr(namesrvAddr);
            consumerListenerForRm.consumer.setConsumeThreadMin(consumeThreadMin);
            //订阅指定topic下tags
            consumerListenerForRm.consumer.subscribe(topic, consumerHandle.getSubExpression());
            /**
             * Consumer第一次启动默认从队列尾部开始消费
             * 如果非第一次启动，那么按照上次消费的位置继续消费
             */
            consumerListenerForRm.consumer.setConsumeFromWhere(consumeFromWhere);
            if (consumeFromWhere.equals(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP) &&
                    !stringIsEmpty(consumeTimestamp)) {
                consumerListenerForRm.consumer.setConsumeTimestamp(consumeTimestamp);
            }
            switch (registerConsumeType) {
                case ORDERLY:
                    consumerListenerForRm.consumer.registerMessageListener((MessageListenerOrderly) (msgs, context) -> {
                        MessageExt msg = msgs.get(0);
                        AsyncMsgRM asyncMsgRM = new AsyncMsgRM(msg);
                        boolean ret = consumerHandle.consume(asyncMsgRM);
                        if (!ret) {
                            logger.error("consume MQ failed,msgID:" + msg.getMsgId());
                        }
                        return ret ? ConsumeOrderlyStatus.SUCCESS :
                                ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    });
                    break;
                case CONCURRENTLY:
                default:
                    consumerListenerForRm.consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                        MessageExt msg = msgs.get(0);
                        AsyncMsgRM asyncMsgRM = new AsyncMsgRM(msg);
                        boolean ret = consumerHandle.consume(asyncMsgRM);
                        if (!ret) {
                            logger.error("consume MQ failed,msgID:" + msg.getMsgId());
                        } else {
                            logger.debug("consume MQ,msg:" + msg.toString() + ",handle result:success.");
                        }
                        return ret ? ConsumeConcurrentlyStatus.CONSUME_SUCCESS :
                                ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    });
                    break;
            }


            return consumerListenerForRm;
        }

    }



    public enum RegisterConsumeType {
        ORDERLY, CONCURRENTLY,
    }



    /**
     * Listener启动
     */
    public void start() throws IOException, MQClientException {
        consumer.start();
        System.out.println("consumerListenerForRm init");
    }


    @PreDestroy
    public void close() {
        this.consumer.shutdown();
        System.out.println("consumerListenerForRm shutdown");
    }



}
