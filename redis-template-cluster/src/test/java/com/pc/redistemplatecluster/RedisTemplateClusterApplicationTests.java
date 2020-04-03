package com.pc.redistemplatecluster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.pc.redistemplatecluster.redisTemplate.RedisUtil;
/**
 * 使用原生jedis集群，就不能使用redisTemplate
 *
 * 其实springboot的redisTemplate底层用的还是jedisCluster
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTemplateClusterApplicationTests {

    @Autowired
    RedisUtil redisUtil;


    @Autowired
    RedisTemplate redisTemplate;



    @Test
    public void testSemaphoreLock() {
        for (int i = 0; i < 1000; i++) {

            Thread t = new Thread(() -> {
//                System.out.println(Thread.currentThread().getName() + " 启动运行");

                while(true){
                    String indentifier = redisUtil.acquireSemaphore("sem_name", 2, 1000*60);
                    if (indentifier == null) {
                        System.out.println(Thread.currentThread().getName() + " 获取信号量失败，取消任务. Semaphore  Semaphore  Semaphore  Semaphore");
                    }else{
                        System.out.println(Thread.currentThread().getName() + "  执行任务");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        redisUtil.releaseSemaphore("sem_name", indentifier);
                        break;
                    }
                }
            });

            t.start();

        }

        System.out.println();
    }

    @Test
    public void testCluster() {
        redisUtil.del("ddd");
        redisUtil.set("ddd","redisTemplate");
        System.out.println();
    }


    @Test
    public void testLock() throws InterruptedException {

        redisUtil.lock("lock",20);

        TimeUnit.SECONDS.sleep(10);


        System.out.println(redisUtil.lock("lock",20));

        System.out.println(redisUtil.release("lock"));

        TimeUnit.SECONDS.sleep(11);
        System.out.println(redisUtil.lock("lock",20));
    }


    @Test
    public void testList() {
        List<String> list = Arrays.asList("aa","bb","cc");

        redisUtil.lSet("testList",list);

        List<String> list1 = redisUtil.lGet("testList",0,list.size());

        System.out.println(list1);

    }

    @Test
    public void transaction() {
        redisTemplate.multi();

        redisTemplate.opsForValue().set("step1","step1");
        redisTemplate.opsForValue().set("step2","step2");
        redisTemplate.opsForValue().set("step3","step3");


        redisTemplate.exec();




    }

}
