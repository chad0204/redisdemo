package com.pc.redisdemo;

import com.pc.redisdemo.redisTemplate.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 使用原生jedis集群，就不能使用redisTemplate
 *
 * 其实springboot的redisTemplate底层用的还是jedisCluster
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisdemoApplicationTests {

    @Autowired
    RedisUtil redisUtil;



//    @Test
//    public void testSemaphoreLock() {
//        for (int i = 0; i < 1000; i++) {
//
//            Thread t = new Thread(() -> {
////                System.out.println(Thread.currentThread().getName() + " 启动运行");
//
//                while(true){
//                    String indentifier = redisUtil.acquireSemaphore("sem_name", 2, 1000*60);
//                    if (indentifier == null) {
//                        System.out.println(Thread.currentThread().getName() + " 获取信号量失败，取消任务. Semaphore  Semaphore  Semaphore  Semaphore");
//                    }else{
//                        System.out.println(Thread.currentThread().getName() + "  执行任务");
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        redisUtil.releaseSemaphore("sem_name", indentifier);
//                        break;
//                    }
//                }
//            });
//
//            t.start();
//
//        }
//
//        System.out.println();
//    }

    @Test
    public void testCluster() {
        redisUtil.del("ddd");
        redisUtil.set("ddd","redisTemplate");
        System.out.println();
    }



}
