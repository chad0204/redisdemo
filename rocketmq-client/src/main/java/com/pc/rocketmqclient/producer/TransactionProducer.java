package com.pc.rocketmqclient.producer;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

/**
 * 确保如果本地事务执行失败，那么可以回滚broker，不让其将消息发送出去
 */

@Component
public class TransactionProducer {

    private static Logger logger = LoggerFactory.getLogger(TransactionProducer.class);

    private TransactionMQProducer producer;

    @PostConstruct
    public void init() throws MQClientException {

        TransactionListener transactionListener = new TransactionListenerImpl();
        producer = new TransactionMQProducer("transaction_group");

        //事务状态回查异步线程池
        ExecutorService executorService =
                new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), r -> {
            Thread thread = new Thread(r);
            //一般自定义线程池的时候，定义名称
            thread.setName("client-transaction-msg-check-thread");
            return thread;
        });


        String namesrvAddr = "localhost:9876";
        if (StringUtils.isEmpty(namesrvAddr)) {
            logger.error("namesrvAddr is empty.");
            return;
        }
        producer.setNamesrvAddr(namesrvAddr);
        producer.setInstanceName("transactionRocketDemo");

        //注册线程池
        producer.setExecutorService(executorService);
        //注册回查监听
        producer.setTransactionListener(transactionListener);
        producer.start();

    }


    public TransactionSendResult sendTransaction(String topic, String tagName, String msgContent, Long id) {

        Message msg = new Message(topic, tagName,
                msgContent.getBytes());
        try {
            return producer.sendMessageInTransaction(msg, id);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return null;
    }

}


class TransactionListenerImpl implements TransactionListener {

    private AtomicInteger transactionIndex = new AtomicInteger(0);

    /**
     * 消息发送完执行本地事务
     *
     * @param msg
     * @param arg
     * @return
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {


        //TODO 可以根据arg（比如是订单id），执行本地事务，获取本地事务执行状态status
        //COMMIT_MESSAGE，broker将消息发给consumer
        //UNKNOW，broker回查producer
        //ROLLBACK_MESSAGE，broker删除消息
        int status = Integer.valueOf(arg.toString());


        switch (status) {
            case 0:
                return LocalTransactionState.UNKNOW;//触发checkLocalTransaction，broker会定时回查，大概30s
            case 1:
                return LocalTransactionState.COMMIT_MESSAGE;//broker执行commit，consumer进行消费
            case 2:
                return LocalTransactionState.ROLLBACK_MESSAGE;//broker删除消息
        }

        return LocalTransactionState.UNKNOW;

    }

    /**
     * broker端回查本地事务
     *
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        //查询本地事务的执行情况，可以到数据库查等等，拿到结果，返回给broker，broker根据结果回滚或提交消息或再次回查。


        if (transactionIndex.getAndIncrement() > 15) {//限制回查次数，默认15

            System.out.println("超过回查次数");
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

//        return LocalTransactionState.UNKNOW;

//        return LocalTransactionState.COMMIT_MESSAGE;

        return LocalTransactionState.ROLLBACK_MESSAGE;


    }
}