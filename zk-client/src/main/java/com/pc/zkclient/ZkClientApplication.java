package com.pc.zkclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ZkClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkClientApplication.class, args);
    }


}
