package com.pc.mybatisdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pc.mybatisdemo.mapper") //扫描的mapper，或者在XXMapper接口上加上注解@Mapper
public class MybatisDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MybatisDemoApplication.class, args);
	}

}

//https://github.com/mybatis/mybatis-3.git


