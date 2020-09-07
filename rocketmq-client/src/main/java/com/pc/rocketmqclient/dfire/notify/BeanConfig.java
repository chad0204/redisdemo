package com.pc.rocketmqclient.dfire.notify;

import com.pc.rocketmqclient.dfire.message.client.consumer.ConsumerListenerForRm;
import com.pc.rocketmqclient.dfire.message.client.consumer.support.ConsumerCallBack;
import com.pc.rocketmqclient.dfire.message.client.consumer.support.MultiConsumerHandle;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author pengchao
 * @date 15:38 2020-09-04
 */
@Configuration
public class BeanConfig {

    @Bean
    public SimpleCallback simpleCallback() {
        return new SimpleCallback();
    }

    @Bean
    public Simple1Callback simple1Callback() {
        return new Simple1Callback();
    }

    @Bean
    public MultiConsumerHandle s1MultiConsumerHandle() {
        List<ConsumerCallBack> callbackList  = Arrays.asList(simple1Callback(),simpleCallback());
        return new MultiConsumerHandle(callbackList);
    }

    @Bean
    public ConsumerListenerForRm consumerListenerForRm() throws MQClientException, IOException {
        ConsumerListenerForRm consumerListenerForRm = ConsumerListenerForRm.newBuilder()
                .setConsumerGroup("c_dfire_topic_group")
                .setConsumerHandle(s1MultiConsumerHandle())
                .setNamesrvAddr("localhost:9876")
                .setTopic("dfire_topic")
                .build();
        consumerListenerForRm.start();
        return consumerListenerForRm;
    }






}
