package com.pc.redisredissionsingle.redission;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @author dongxie
 * @date 17:06 2020-04-03
 */
@Configuration
@Slf4j
public class RedissionConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private Integer port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxTotal;


    @PostConstruct
    public void init() {
        log.info("ids redis host:{}, port:{}", this.host, this.port);
    }


    @Bean
    public RedissonClient getRedisson() {
        Config config = new Config();
        config.useSingleServer().setAddress(host).setPassword(password).setTimeout(timeout);
        return Redisson.create(config);
    }

}
