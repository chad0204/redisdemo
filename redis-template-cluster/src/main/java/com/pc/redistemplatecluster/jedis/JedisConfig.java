//package com.pc.redistemplatecluster.jedis;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.CollectionUtils;
//import redis.clients.jedis.HostAndPort;
//import redis.clients.jedis.JedisCluster;
//import redis.clients.jedis.JedisPoolConfig;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// *
// *
// * @author dongxie
// * @date 16:16 2020-04-02
// */
//@Configuration
//@Slf4j
//public class JedisConfig {
//
//    @Value("${spring.redis.cluster.nodes}")
//    private Set<String> redisNodes;
//
//    @Value("${spring.redis.jedis.pool.max-active}")
//    private int maxTotal;
//
//    @Value("${spring.redis.jedis.pool.max-idle}")
//    private int maxIdle;
//
//    @Value("${spring.redis.jedis.pool.min-idle}")
//    private int minIdle;
//
//    @Value("${spring.redis.timeout}")
//    private int timeout;
//
//    @Value("${spring.redis.cluster.max-redirects}")
//    private int maxAttempts;
//
//
//
//    @Bean
//    public JedisCluster jedisCluster(){
//
//        if (CollectionUtils.isEmpty(redisNodes)) {
//            throw new RuntimeException();
//        }
//
//        //解析节点
//        Set<HostAndPort> nodes = new HashSet<>();
//        for (String node : redisNodes) {
//            String[] arr = node.split(":");
//            HostAndPort hostAndPort = new HostAndPort(arr[0],Integer.parseInt(arr[1]));
//            nodes.add(hostAndPort);
//        }
//
//        //配置连接池
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setMaxTotal(maxTotal);
//        jedisPoolConfig.setMaxIdle(maxIdle);
//        jedisPoolConfig.setMinIdle(minIdle);
//
//
//        // 创建jedisCluster，传入节点列表和连接池配置
//        JedisCluster cluster = new JedisCluster(nodes, timeout,maxAttempts,jedisPoolConfig);
//
//        log.info("finish jedis cluster initailization");
//        return cluster;
//
//    }
//
//
//}