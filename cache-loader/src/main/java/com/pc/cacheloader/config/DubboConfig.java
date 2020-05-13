package com.pc.cacheloader.config;


import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@DubboComponentScan(basePackages = {"com.pc.cacheloader.providers"})
public class DubboConfig {

    @Autowired
    private CuratorConfig curatorConfig;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("data-loader");
        return applicationConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(curatorConfig.getHost());
        registryConfig.setProtocol("zookeeper");
//        if (applicationContext.getEnvironment().getActiveProfiles()[0].equals("local")) {
//            registryConfig.setRegister(false);
//        } else {
            registryConfig.setRegister(true);
//        }

        return registryConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setTimeout(3000);
        return consumerConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setPayload(883886080);
        return protocolConfig;
    }

}
