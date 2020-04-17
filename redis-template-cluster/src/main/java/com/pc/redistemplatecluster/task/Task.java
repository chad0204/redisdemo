package com.pc.redistemplatecluster.task;

import com.pc.redistemplatecluster.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务，模拟多客户端，用于测试分布式锁
 *
 * @author dongxie
 * @date 11:02 2020-04-14
 */
@Component
@EnableScheduling//定时任务需要设置多线程执行
@Configuration
public class Task {

    @Autowired
    private RedisUtil redisUtil;


    private static final String SPECIFY_TIME = "0 30 12 1/1 * ?";//12点30执行

    //单机测试可以使用周期执行，因为时间是一致的。
    private static final String PUT_TIME = "0 */1 * * * ?";//1分钟一次

    private static final String PERIOD_TIME = "0/30 * * * * ?";//30秒一次


    private static final String REDIS_DISTRIBUTION_LOCK = "redis_distribution_lock";


    @Scheduled(cron = PUT_TIME)//每隔一分钟放一次商品，让商品的新增速度小于消费速度
    public void putGoods() {
        redisUtil.incr("no_lock_goods",3);
        redisUtil.incr("lock_goods",3);
    }


    @Scheduled(cron = "0/5 * * * * ?")
    public void check() {
        Object data = redisUtil.get("no_lock_goods");
        if(data!=null && Integer.valueOf(data.toString())<0) {
            System.out.println(Thread.currentThread().getName()+" ********no lock error"+Integer.valueOf(data.toString()));
        }
        Object data1 = redisUtil.get("lock_goods");
        if(data1!=null && Integer.valueOf(data1.toString())<0) {
            System.out.println(Thread.currentThread().getName()+" ********lock error"+Integer.valueOf(data1.toString()));
        }

    }





    //多任务同时跑，但是Scheduled默认是单线程的，所以任务还是依次执行，所以需要配置成多线程,由于配置了10个线程，所以最多10个任务
    @Scheduled(cron = PERIOD_TIME)
    public void handle1() {
//        testTask("handle1");
        testSynchronized("handle1");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle2() {
//        testTask("handle2");
        testSynchronized("handle2");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle3() {
//        testTask("handle3");
        testSynchronized("handle3");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle4() {
//        testTask("handle4");
        testSynchronized("handle4");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle5() {
//        testTask("handle5");
        testSynchronized("handle5");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle6() {
        testNoSynchronized("handle6");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle7() {
        testNoSynchronized("handle7");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle8() {
        testNoSynchronized("handle8");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle9() {
        testNoSynchronized("handle9");
    }
    @Scheduled(cron = PERIOD_TIME)
    public void handle10() {
        testNoSynchronized("handle10");
    }


    /**
     * 如果是控制定时任务只有一个执行，那么没有必要释放锁，同时设置超时时间很短，不需要重试
     * @param uuid
     */
    public void testTask(String uuid) {
        //锁类型1：有缺陷
//        if(redisUtil.tryDistributeLock(REDIS_DISTRIBUTION_LOCK,10, TimeUnit.SECONDS)) {
//            System.out.println(Thread.currentThread().getName()+" succeed lock");
//        } else {
//            System.out.println(Thread.currentThread().getName()+" failed lock");
//        }

        //锁类型2：有缺陷
//        if(redisUtil.tryDistributeLock(REDIS_DISTRIBUTION_LOCK, 1000*10, 1)) {//由于是定时任务，所以不用重试，超时时间设置最短
//            System.out.println(Thread.currentThread().getName()+" succeed lock");
//            redisUtil.releaseDistributedLock(REDIS_DISTRIBUTION_LOCK);
//        } else {
//            System.out.println(Thread.currentThread().getName()+" failed Lock");
//        }

        //锁类型3：完美
        if(redisUtil.tryLock(REDIS_DISTRIBUTION_LOCK, uuid, 10,1)) {
            try {
                System.out.println(Thread.currentThread().getName()+"-"+uuid+" succeed lock");
            } catch (Exception e) {
                //控制定时任务执行一次。如果执行完就释放，当执行速度很快时，那么别的客户端会拿到锁开始执行，会导致定时任务跑多次。
                //应该是执行报错或者超时才会释放
                redisUtil.releaseLock(REDIS_DISTRIBUTION_LOCK,uuid);
            }
        } else {
            System.out.println(Thread.currentThread().getName()+" failed lock");
        }

    }

    /**
     * 如果是控制资源的串型访问，比如多台服务器从同时查询redis，mysql中的数据，需要在执行结束后立即释放锁，同时没有获取到锁的客户端也需要自旋重试。
     *
     * data为商品数量，初始为5,开启10个客户端抢购，当商品为0时，则不能减1。
     *
     * @param uuid
     */
    public void testSynchronized(String uuid) {
        if(redisUtil.tryLock(REDIS_DISTRIBUTION_LOCK, uuid, 10,10000)) {
            try {
                Object data = redisUtil.get("lock_goods");
                try {
                    //随机休眠,放大错误
                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(data!=null && Integer.valueOf(data.toString())>0) {
//                    System.out.println(Thread.currentThread().getName()+" decr 1");
                    redisUtil.decr("lock_goods",1);
                } else {
//                    System.out.println(Thread.currentThread().getName()+" lock_goods empty");
                }
                redisUtil.releaseLock(REDIS_DISTRIBUTION_LOCK,uuid);//执行完立即释放锁，让其他客户端执行
            } catch (Exception e) {
                //应该是执行报错或者超时才会释放
                redisUtil.releaseLock(REDIS_DISTRIBUTION_LOCK,uuid);
            } finally {
                redisUtil.releaseLock(REDIS_DISTRIBUTION_LOCK,uuid);
            }
        } else {
            System.out.println(Thread.currentThread().getName()+" failed lock");
        }
    }

    /**
     * 没有分布式锁,会出现no_lock_goods已经为0，但还是减1的情况,导致data<0
     * @param uuid
     */
    public void testNoSynchronized(String uuid) {
        //每个客户端进来减1
        Object data = redisUtil.get("no_lock_goods");
        try {
            //随机休眠,放大错误
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(data!=null && Integer.valueOf(data.toString())>0) {
//            System.out.println(Thread.currentThread().getName()+" decr 1");
            redisUtil.decr("no_lock_goods",1);
        } else {
//            System.out.println(Thread.currentThread().getName()+" no_lock_goods empty");
        }
    }


}
