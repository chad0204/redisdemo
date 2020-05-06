package com.pc.kafka.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author dongxie
 * @date 21:38 2020-05-06
 */
@Component
public class ConsumerListener {

    @KafkaListener(topics = "first")
    public void onMessage(String message){
        System.out.println(message);
    }
}
