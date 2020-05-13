package com.pc.cacheloader;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pc.cacheloader.mapper") //扫描的mapper
public class CacheLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheLoaderApplication.class, args);
    }

}
