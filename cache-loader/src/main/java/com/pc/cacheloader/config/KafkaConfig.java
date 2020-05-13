package com.pc.cacheloader.config;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaConfig.class)
@ConfigurationProperties(value = "kafka")
@Data
public class KafkaConfig {
    private String servers;
    private String groupId;
    private String autoOffsetReset;
    private Integer concurrency;

    public Map<String, Object> getNormalConsumerProperties() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        params.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        params.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        params.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        params.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return params;
    }

    //不自动提交/手动提交OFFSET
    public Map<String, Object> getInnerConsumerProperties() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        params.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        params.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        params.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        params.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        params.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return params;
    }

    private Properties getProducerProps() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put("buffer.memory", 33554432);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean(name = "kafkaNormalListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaNormalListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory(this.getNormalConsumerProperties()));
        factory.setConcurrency(concurrency);
        return factory;
    }

    @Bean(name = "kafkaInnerListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaInnerListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory(this.getInnerConsumerProperties()));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(30);
        return factory;
    }


    @Bean(name = "kafkaProducer")
    public KafkaTemplate<String, String> kafkaTemplate() {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate(kafkaProducerFactory());
        kafkaTemplate.setProducerListener(kafkaProducerListener());
        return kafkaTemplate;
    }

    private ProducerListener<String, String> kafkaProducerListener() {
        return new LoggingProducerListener();
    }

    private ProducerFactory<String, String> kafkaProducerFactory() {
        DefaultKafkaProducerFactory<String, String> factory = new DefaultKafkaProducerFactory(this.getProducerProps());
        return factory;
    }
}


