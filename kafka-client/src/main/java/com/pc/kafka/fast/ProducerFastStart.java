package com.pc.kafka.fast;

import org.apache.kafka.clients.producer.*;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author pengchao
 * @date 09:31 2020-07-24
 */
public class ProducerFastStart {

    private static final String brokerList = "localhost:9092";
    private static final String topic = "fast_topic";


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties properties = new Properties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,brokerList);

        //这是client.id,不设置也会自动生成
        properties.put(ProducerConfig.CLIENT_ID_CONFIG,"producer.client.id.demo");

        //配置生产者客户端参数，并创建Producer
        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        //构建所需要发送的消息
        ProducerRecord<String,String> record = new ProducerRecord<>(topic,"hello fast kafka!");


        //同步发送可以直接调用get()方法进行阻塞，知道成功或者异常，异常需要外层业务处理
        Future<RecordMetadata> future = producer.send(record);
        RecordMetadata metadata = future.get();//也可以超时等待



        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            }
        });

        producer.close();
    }


}
