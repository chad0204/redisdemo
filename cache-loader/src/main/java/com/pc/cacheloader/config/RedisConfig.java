package com.pc.cacheloader.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 */
@EnableConfigurationProperties(RedisConfig.class)
@ConfigurationProperties(prefix = "redis")
@Configuration
@Data
public class RedisConfig {
    private String host;
    private int port;
    private int database;
    private int timeout;
    private int maxPool;
    private String password;

    @Bean(name = "loaderRedisConnectionFactory")
    public RedisConnectionFactory predictRedisConnectionFactory() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxPool);
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(host);
        factory.setPort(port);
        if (StringUtils.hasText(password)) {
            factory.setPassword(password);
        }
        factory.setDatabase(database);
        factory.setTimeout(timeout);
        factory.setUsePool(true);
        factory.setPoolConfig(config);
        return factory;
    }

    @Bean(name = "loaderRedisTemplate")
    public StringRedisTemplate predictRedisTemplate(@Qualifier("loaderRedisConnectionFactory") RedisConnectionFactory predictRedisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(predictRedisConnectionFactory);
        return template;
    }


}
