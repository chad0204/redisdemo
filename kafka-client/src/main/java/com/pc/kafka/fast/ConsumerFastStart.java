package com.pc.kafka.fast;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 *
 * @author pengchao
 * @date 09:55 2020-07-24
 */
public class ConsumerFastStart {

    private static final String brokerList = "localhost:9092";
    private static final String topic = "fast_topic";


    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("bootstrap.servers",brokerList);
        properties.put("group.id","default_consumer_group");//消费者组

        //配置消费者参数，创建消费者
        KafkaConsumer<String,String> consumer = new KafkaConsumer<>(properties);

        //订阅
        consumer.subscribe(Collections.singleton(topic));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            if(records!=null && !records.isEmpty()) {
                records.forEach(e-> System.out.println(e.value()));
            }
        }

    }
}
