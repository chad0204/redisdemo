package com.pc.redistemplatecluster.redisTemplate;

import org.springframework.stereotype.Component;

/**
 *
 * 消息接收类
 * @author dongxie
 * @date 09:23 2020-04-14
 */
@Component
public class MessageReceiveTwo {

    public void receiveMessage(String message) {
        System.out.println("MessageReceiveTwo receive:"+message);
    }

}
