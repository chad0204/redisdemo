package com.pc.rocketmqclient.producer;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 生产者
 *
 * @author pengchao
 * @date 09:52 2020-05-30
 */
@Component
public class Producer {
    private static Logger logger = LoggerFactory.getLogger(Producer.class);


    private DefaultMQProducer producer;

    @PostConstruct
    public void init() {

        logger.info("producer init.");
        producer = new DefaultMQProducer("producer_group");
        String namesrvAddr = "localhost:9876";
        if (StringUtils.isEmpty(namesrvAddr)) {
            logger.error("namesrvAddr is empty.");
            return;
        }
        producer.setNamesrvAddr(namesrvAddr);
        producer.setInstanceName("rocketmq-client");

        /**
         * 这里的失败是producer->broker的失败，如果返回状态不是send_ok,那么producer会在内部自己重试
         */
        producer.setRetryTimesWhenSendFailed(2);//设置失败重试的次数


        producer.setRetryTimesWhenSendAsyncFailed(1);//设置异步失败重试的次数

        //设置发送超时时间，默认是3000
        producer.setSendMsgTimeout(6000);

        try {
            producer.start();
        } catch (MQClientException e) {
            logger.error("producer start error. {}", e);
        }
    }
    @PreDestroy
    public void destroy() {
        logger.info("producer destroy.");
        if (producer != null) {
            producer.shutdown();
        }
    }


    /**
     * 普通消息
     * @param topic
     * @param tagName
     * @param msgContent
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     */
    public SendResult sendNormal(String topic, String tagName, String msgContent) throws InterruptedException, RemotingException, MQClientException {
        try {
            Message message = new Message(topic,tagName,msgContent.getBytes());
            return producer.send(message);
        } catch (MQBrokerException e) {
            logger.error("producer send message error: [{}]", e);
        }
        return null;
    }

    /**
     *
     * 顺序消息
     * @param topic
     * @param tagName
     * @param key
     * @param msgContent
     * @param orderId
     * @return
     */
    public SendResult sendOrderly(String topic, String tagName, String key, String msgContent, Long orderId) throws InterruptedException, RemotingException, MQClientException {
        try {
            Message message = new Message(topic,tagName,key,msgContent.getBytes());
            return producer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    //根据参数取模后拿到队列号（如果非long参数可以使用参数hashCode）,
                    // 这样相同arg一定会发到同一个队列。如相同订单号的，创建、付款、物流、完成等操作
                    Long value = (Long) arg;
                    long index = value % mqs.size();
                    return mqs.get((int) index);//选择队列号
                }
            },orderId);//这里的orderId就是select()方法的参数arg
        } catch (MQBrokerException e) {
            logger.error("producer send message error: [{}]", e);
        }
        return null;
    }


    /**
     *
     *  保证消息可靠性
     * @param topic
     * @param tagName
     * @param msgContent
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     */
    public SendResult sendSafe(String topic, String tagName, String msgContent) throws InterruptedException, RemotingException, MQClientException {
        try {
            Message message = new Message(topic,tagName,msgContent.getBytes());
            return producer.send(message);
        } catch (MQBrokerException e) {
            logger.error("producer send message error: [{}]", e);
        }
        return null;
    }

    /**
     * 异步发送
     * @param topic
     * @param tagName
     * @param msgContent
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     */
    public SendResult sendSafeAysn(String topic, String tagName, String msgContent) throws InterruptedException, RemotingException, MQClientException {
        try {
            Message msg = new Message(topic, tagName, transferObjectToByte(msgContent));
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("异步发送：" + sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    //异常处理，比如重试，发邮件等
                }
            });
        } catch (Exception e) {
            logger.error("producer send message error: [{}]", e);
        }
        return null;
    }





    public static byte[] transferObjectToByte(java.lang.Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            logger.error("transfer object to byte error: [{}]", e);
        }
        return bytes;
    }




}
