package com.pc.rocketmqclient.rest;

import com.pc.rocketmqclient.model.MQConstants;
import com.pc.rocketmqclient.model.Order;
import com.pc.rocketmqclient.producer.Producer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author pengchao
 * @date 12:55 2020-05-31
 */
@RestController
public class Controller {

    @Autowired
    private Producer producer;

    @RequestMapping("/send/normal")
    public Object sendOrder() {
        Order.getOrderList().forEach(order -> {
            try {
                SendResult result = producer
                        .sendNormal(MQConstants.NORMAL_TOPIC,MQConstants.NORMAL_TOPIC,order.toString());
//                System.out.println(result.getMessageQueue().getQueueId() + "===" + order.getOrderId());
            } catch (InterruptedException | RemotingException | MQClientException e) {
                e.printStackTrace();
            }
        });
        return "success";

    }


    @RequestMapping("/send/orderly")
    public Object sendOrderly() {

        Order.getOrderList().forEach(order -> {
            try {
                SendResult result = producer
                        .sendOrderly(MQConstants.ORDERLY_TOPIC,MQConstants.ORDERLY_TAG_1,"orderly_key",order.toString(),order.getOrderId());
                System.out.println(result.getMessageQueue().getQueueId() + "===" + order.getOrderId());
            } catch (InterruptedException | RemotingException | MQClientException e) {
                e.printStackTrace();
            }
        });
        return "success";

    }

    @RequestMapping("/send/safe")
    public Object sendSafe() {
        try {
            for(int i=0; i< 100; i++) {
                SendResult result = producer
                        .sendSafe(MQConstants.SAFE_TOPIC,MQConstants.SAFE_TAG,i+"");
//                System.out.println(result.getMessageQueue().getQueueId() + "===" + i);
            }
        } catch (InterruptedException | RemotingException | MQClientException e) {
            e.printStackTrace();
        }
        return "success";

    }
}
