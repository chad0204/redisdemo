package com.pc.redisdemo.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集群redis配置,也可以直接使用redisTemplate,只要redis是集群模式，redisTemplate就是集群访问
 *
 * @author pengchao
 * @since 13:54 2019-10-31
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisClusterConfig {

    private final RedisProperties redisProperties;

    public RedisClusterConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }


    @Bean
    public JedisCluster jedisCluster(){
        List<String> redisNodes= redisProperties.getCluster().getNodes();
        Set<HostAndPort> nodes = new HashSet<>();
        for (String node : redisNodes) {
            String[] arr = node.split(":");
            HostAndPort hostAndPort = new HostAndPort(arr[0],Integer.parseInt(arr[1]));
            nodes.add(hostAndPort);
        }

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();

        config.setMaxTotal(redisProperties.getJedis().getPool().getMaxActive());
        config.setMaxIdle(redisProperties.getJedis().getPool().getMaxIdle());
        config.setMaxWaitMillis(redisProperties.getJedis().getPool().getMaxWait().toMillis());
        config.setTestOnBorrow(true);

        return new JedisCluster(nodes, (int) redisProperties.getTimeout().getSeconds(),
                redisProperties.getCluster().getMaxRedirects(),config);


    }
}
