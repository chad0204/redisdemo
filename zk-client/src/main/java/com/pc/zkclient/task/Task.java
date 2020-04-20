package com.pc.zkclient.task;

import com.pc.zkclient.util.ZkClientTemplate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务，模拟多客户端
 *
 * @author dongxie
 * @date 21:22 2020-04-19
 */
@Component
@EnableScheduling//定时任务需要设置多线程执行
@Configuration
public class Task {


    private static final String SPECIFY_TIME = "0 30 12 1/1 * ?";//12点30执行

    //单机测试可以使用周期执行，因为时间是一致的。
    private static final String PUT_TIME = "0 */1 * * * ?";//1分钟一次

    private static final String PERIOD_TIME = "0/30 * * * * ?";//30秒一次


    private static final String EXCLUSIVE_LOCK = "/lock/exclusive";//独占锁

    private static final String SHARED_LOCK = "/lock/shared";//共享锁


    private static final String REDIS_DATA = "goods";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ZkClientTemplate zkClientTemplate;

    @Autowired
    private CuratorFramework client;


    @Scheduled(cron = PUT_TIME)//每隔一分钟放一次商品，让商品的新增速度小于消费速度
    public void putGoods() {
        redisTemplate.opsForValue().increment(REDIS_DATA,3);
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void check() {
        Object data = redisTemplate.opsForValue().get(REDIS_DATA);
        if(data!=null && Integer.valueOf(data.toString())<0) {
            System.out.println(Thread.currentThread().getName()+" ********lock error"+Integer.valueOf(data.toString()));
        }
    }


    //多任务同时跑，但是Scheduled默认是单线程的，所以任务还是依次执行，所以需要配置成多线程,由于配置了10个线程，所以最多10个任务
    @Scheduled(cron = PERIOD_TIME)
    public void handle1() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle2() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle3() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle4() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle5() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle6() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle7() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle8() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle9() {
        getSharedData();
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle10() {
        getSharedData();
    }


    //控制只有一台服务器可以执行
    public void singletonTask() {
        if(zkClientTemplate.lock(EXCLUSIVE_LOCK)) {
            System.out.println("try lock succeed");
            zkClientTemplate.release(EXCLUSIVE_LOCK);//要手动删除，否则只有在客户端与zk断开时才会删除
        } else {
            System.out.println("try lock failed");
        }
    }

    //控制资源的共享,使用InterProcessMutex
    public void getSharedData() {
        InterProcessMutex mutex = new InterProcessMutex(client,EXCLUSIVE_LOCK);
        if(zkClientTemplate.mutexLock(mutex,10000,TimeUnit.MILLISECONDS)) {
            try {
                Object data = redisTemplate.opsForValue().get(REDIS_DATA);//获取共享资源
                try {
                    //随机休眠,放大错误
                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(data!=null && Integer.valueOf(data.toString())>0) {
                    System.out.println(Thread.currentThread().getName()+" decr 1");
                    redisTemplate.opsForValue().decrement(REDIS_DATA,1);
                } else {
                    System.out.println(Thread.currentThread().getName()+" goods is empty");
                }
            } finally {
                zkClientTemplate.mutexRelease(mutex);
            }
        } else {
            System.out.println("try lock failed");
        }

    }









}
