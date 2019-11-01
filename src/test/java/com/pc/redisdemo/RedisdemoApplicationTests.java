package com.pc.redisdemo;

import com.pc.redisdemo.util.RedisClusterUtil;
import com.pc.redisdemo.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisdemoApplicationTests {


    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisClusterUtil redisClient;






//    private static final int THREAD_NUM = Math.min(4, Runtime.getRuntime().availableProcessors() * 2 + 1);

//    private ExecutorService executorService = new ThreadPoolExecutor(25, 25, 0L, TimeUnit.MILLISECONDS,
//            new LinkedBlockingQueue<>());



    @Test
    public void testRedisUtil() {

        redisUtil.set("a","bbbb");

        redisUtil.set("bc","bbbb");

        redisUtil.set("efg","bbbb");

        String value = (String) redisUtil.get("normalKey");

        for(int i=0;i<20;i++) {
            redisUtil.del("cc"+i);
        }
    }


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


        for(int i = 0 ;i < 10 ; i ++) {
            redisClient.shardHset("clusterHash","filed"+i,"value");
        }




        String value = redisClient.shardHget("clusterHash","filed1");


        System.out.println();
    }

}
