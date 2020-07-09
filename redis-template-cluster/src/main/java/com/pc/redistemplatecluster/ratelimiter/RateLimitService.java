package com.pc.redistemplatecluster.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试单机限流
 *
 *       限流操作
 *          1.首先过滤出vip用户，或者丢弃一些问题请求
 *          2.放入mq中削峰,超过一定数量就丢弃
 *          3.
 *
 *
 *
 * @author pengchao
 * @date 22:04 2020-07-09
 */
public class RateLimitService {


    private static AtomicInteger acquireCount = new AtomicInteger(0);
    private static final Object OBJECT = new Object();

    //每秒只发出50个令牌
    private  static RateLimiter rateLimiter = RateLimiter.create(50.0);


    private boolean tryAcquire() {
        // 等待1秒钟如果未能获取到许可证就返回false，否则返回true
        return rateLimiter.tryAcquire(1);

//        return rateLimiter.tryAcquire(1, 1000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws InterruptedException {

        RateLimitService accessLimitService = new RateLimitService();
        ExecutorService executorService = Executors.newCachedThreadPool();



        TimeUnit.SECONDS.sleep(10);//阻塞一会，让令牌桶攒满50个



        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (accessLimitService.tryAcquire()) {

                        if(System.currentTimeMillis()-start>1000) {
//                            System.out.println("==========超过1s=============");
                        } else {

                            //平滑限流，可以看出没20ms（1000/50）允许一次
                            System.out.println("获取许可证，执行业务逻辑。时间："+(System.currentTimeMillis()-start));

                            //计数
                            System.out.println(acquireCount.incrementAndGet());
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException ex) {
                                //
                            }
                        }
                    } else {
                        if(System.currentTimeMillis()-start>1000) {
//                            System.out.println("==========超过1s=============");
                        } else {
                            System.err.println("未获取到许可证，请求可以丢弃。");
                        }

                    }
                }
            });
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
    }




}
