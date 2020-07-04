package com.pc.logistics.task;

import com.pc.logistics.model.constants.RedisConstants;
import com.pc.logistics.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;


@Component
@EnableScheduling//定时任务需要设置多线程执行
@Configuration
public class OrderTask {

    @Autowired
    private RedisUtil redisUtil;


    AtomicInteger orderId = new AtomicInteger(0);

    Random random = new Random();


    /**
     * 生成订单
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void order() {
        redisUtil.shardHset(RedisConstants.OPTION_KEY,orderId.getAndIncrement()+"",random.nextInt(1000)+"");

        redisUtil.shardHset(RedisConstants.STOCK_KEY,orderId.getAndIncrement()+"",random.nextInt(1000)+"");
    }




    public void run() {

        while (true) {
            Map<Object, Object> orderMap =  redisUtil.hgetAll(RedisConstants.STOCK_KEY);




        }



    }





}
