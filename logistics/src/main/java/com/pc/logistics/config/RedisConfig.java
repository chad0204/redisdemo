package com.pc.logistics.config;

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

}
