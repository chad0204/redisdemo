package com.pc.redistemplatecluster.redisTemplate;

import org.springframework.stereotype.Component;

/**
 * 消息接收
 * @author dongxie
 * @date 09:23 2020-04-14
 */
@Component
public class MessageReceiveOne {

    public void receiveMessage(String message) {
        System.out.println("MessageReceiveOne receive:"+message);
    }
}
