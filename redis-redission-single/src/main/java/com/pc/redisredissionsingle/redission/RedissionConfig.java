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

    @Value("${redis.ids.host}")
    private String host;

    @Value("${redis.ids.port}")
    private Integer port;

    @Value("${redis.ids.password}")
    private String password;

    @Value("${redis.ids.index}")
    private Integer index;

    @Value("${redis.ids.timeout}")
    private int timeout;

    @Value("${redis.ids.pool.max-total}")
    private int maxTotal;

    @Value("${redis.ids.masterAddress}")
    private String masterAddress;
    @Value("${redis.ids.slaveAddress}")
    private String slaveAddress;
    @Value("${server.env}")
    private String serverEnv;


    @PostConstruct
    public void init() {
        log.info("ids redis host:{}, port:{}", this.host, this.port);
    }


    @Bean
    public RedissonClient getRedisson() {
        Config config = new Config();

        if (!StringUtils.isEmpty(serverEnv) && serverEnv.equals("prod")){
            config.useMasterSlaveServers().setMasterAddress(masterAddress).setPassword(password)
                    .addSlaveAddress(slaveAddress);
        }else {
            config.useSingleServer().setAddress(masterAddress).setPassword(password);
        }
//
        return Redisson.create(config);
    }

}
