package com.pc.rocketmqclient;

import com.pc.rocketmqclient.model.Order;
import com.pc.rocketmqclient.model.MQConstants;
import com.pc.rocketmqclient.producer.Producer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RocketmqClientApplicationTests {


    @Autowired
    private Producer producer;

    @Test
    public void contextLoads() throws InterruptedException {
        Order.getOrderList().forEach(order -> {
            try {
                SendResult result = producer
                        .sendOrderly(MQConstants.ORDERLY_TOPIC,MQConstants.ORDERLY_TAG_1,"orderly_key",order.toString(),order.getOrderId());
                System.out.println(result.getMessageQueue().getQueueId() + "===" + order.getOrderId());
            } catch (InterruptedException | RemotingException | MQClientException e) {
                e.printStackTrace();
            }
        });


        TimeUnit.SECONDS.sleep(5);
    }

}
