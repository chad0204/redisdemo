package com.pc.redistemplatecluster.redisTemplate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 *
 * 单机redis配置，也可配置采用默认的redisTemplate<Object,Object>和stringRedisTemplate<String,String>
 *
 * springboot本身提供了两个RedisTemplate的bean,RedisTemplate<Object, Object>和StringRedisTemplate
 * 这里自定义的RedisTemplate<String, Object>是替代了RedisAutoConfiguration的RedisTemplate<Object, Object>(springboot用@ConditionalOnMissingBean注解，如果已经存在则不进行初始化)
 *
 * @author pengchao
 * @since 10:46 2019-10-21
 */
@Configuration
public class RedisConfig {

    //也可以自定义RedisConnectionFactory
    @Bean(name="redisTemplate")
    @SuppressWarnings("all")
    public StringRedisTemplate redisTemplate(RedisConnectionFactory factory/*也可以自定义*/) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
//        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }


     /**
     * redis消息监听器容器
     * 可以添加多个监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，该消息监听器
     * 通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
     * @param connectionFactory
     * @param listenerAdapter1 表示监听频道的不同订阅者
     * @return
     */
    @Bean
    RedisMessageListenerContainer container2(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter1, MessageListenerAdapter listenerAdapter2){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //订阅多个频道
        container.addMessageListener(listenerAdapter1,new PatternTopic("channel1"));
        container.addMessageListener(listenerAdapter2,new PatternTopic("channel2"));
        container.addMessageListener(listenerAdapter2,new PatternTopic("channel3"));

        //序列化对象（特别注意：发布的时候需要设置序列化；订阅方也需要设置序列化）
        Jackson2JsonRedisSerializer seria = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        seria.setObjectMapper(objectMapper);

        container.setTopicSerializer(seria);
        return container;
    }


    //表示监听一个频道
    @Bean
    MessageListenerAdapter listenerAdapter1(MessageReceiveOne receiver){
        //这个地方 是给messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“MessageReceiveTwo ”
        return new MessageListenerAdapter(receiver,"receiveMessage");
    }

    @Bean
    MessageListenerAdapter listenerAdapter2(MessageReceiveTwo receiver){
        //这个地方 是给messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“MessageReceiveOne ”
        return new MessageListenerAdapter(receiver,"receiveMessage");
    }


}
