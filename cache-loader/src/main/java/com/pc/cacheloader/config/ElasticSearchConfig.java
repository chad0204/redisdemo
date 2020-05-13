package com.pc.cacheloader.config;

import com.alibaba.dubbo.common.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetAddress;

/**
 * es加载类
 */
@Configuration
@EnableConfigurationProperties(ElasticSearchConfig.class)
@ConfigurationProperties(prefix = "es")
@Data
@Slf4j
public class ElasticSearchConfig {

    private String host;

    private Integer port;

    private String cluster;

    private String xpack;

    @Bean
    public TransportClient transportClient() {
        try {
            Settings settings;
            if (StringUtils.isBlank(xpack)) {
                settings = Settings.builder().put("cluster.name", cluster).put("client.transport.sniff", false).build();
            } else {
                settings = Settings.builder().put("cluster.name", cluster).put("client.transport.sniff", false)
                        .put("xpack.security.user", xpack).build();
            }
            return new PreBuiltXPackTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host),
                            port));
        } catch (Exception e) {
            log.error("elastic search client initialized failed {}", e);
            //throw new RuntimeException("elastic load error");
        }
        return null;
    }

}
