package com.pc.zkclient.config;

import com.pc.zkclient.util.ZkClientTemplate;
import lombok.Data;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * zk配置类
 */
@Configuration
@EnableConfigurationProperties(CuratorConfig.class)
@ConfigurationProperties(prefix = "zk")
@Data
public class CuratorConfig {

    private String host;

    int DISCONNECTED = 0;

    int CONNECTED = 1;

    int RECONNECTED = 2;

    @Bean
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new RetryNTimes(3, 1000);//重试策略
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(10000)
                .connectionTimeoutMs(3000)
                .build();
        client.start();
        return client;
    }


}