package com.pc.redistemplatesingle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTemplateSingleApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;

    private final ExecutorService executorService = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    /**
     * 事务，集群下会报错
     *
     * 两种方式
     *
     * 事务内multi之后无法读取，所以也不能根据读取的数据影响事务
     *
     */
    @Test
    public void transaction() {


        //1.开启事务，在同一个 Connection 中执行命令
        redisTemplate.setEnableTransactionSupport(true);
        Object value1 = redisTemplate.opsForValue().get("value");//事务外可以读取
        redisTemplate.multi();

        Object value2 = redisTemplate.opsForValue().get("value");
        if(value2 !=null) {//事务内读不到，也就不能按照啊读取的结果决定事务
            return;
        }

        redisTemplate.opsForValue().set("step1","step1");
        redisTemplate.opsForValue().set("step2","step2");
        redisTemplate.opsForValue().set("step3","step3");
//        int a = 1/0;
        redisTemplate.exec();



        //2.在SessionCallback执行
        SessionCallback sessionCallback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().set("step1","step1");
                operations.opsForValue().set("step2","step2");
                operations.opsForValue().set("step3","step3");
                operations.exec();
                return null;
            }
        };
        redisTemplate.execute(sessionCallback);

    }


    @Test
    public void transactionWithWatch() throws InterruptedException {

        String key = "count";


        for(int i=0;i<10;i++) {
            //提交10个线程
            executorService.execute(() -> redisTemplate.execute(new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    List<Object> result = null;
                    for(;;) {//自旋
                        int count = 0;
                        //如果去掉watch，那么每次修改都会成功，并发修改导致结果被覆盖，最后count<10
                        operations.watch(key);//如果被并发修改会使下面的提交失败
                        if (operations.opsForValue().get(key) != null) {
                            count = (Integer) Objects.requireNonNull(operations.opsForValue().get(key));
                        }
                        count = count + 1;

                        operations.multi();
                        operations.opsForValue().set(key, count);
                        try {
                            result = operations.exec();//提交事务,watch检测到中断这里提交结果为空

                        } catch (Exception e) { //如果key被改变,提交事务时这里会报异常
                            System.out.println("exec error");
                        }

                        if(!CollectionUtils.isEmpty(result)) {
                            break;//直到没有并发修改，提交成功才结束循环
                        }
                    }
                    return result;
                }
            }));
        }



        TimeUnit.SECONDS.sleep(5);//防止测试线程先挂


    }


    @Test
    public void testIncr() throws InterruptedException {

        Object value = redisTemplate.opsForValue().get("incr");

        if(value!=null && (Integer)value >=1) {
            System.out.println("锁住了");
            return;
        }

        redisTemplate.opsForValue().increment("incr",1);
        redisTemplate.expire("incr",100,TimeUnit.SECONDS);
        System.out.println("加锁");


    }


    @Test
    public void testHash() throws InterruptedException {

//        redisTemplate.opsForHash().put("shunfeng".getBytes(),"1001".getBytes(),"aa".getBytes());
//        redisTemplate.opsForHash().put("shunfeng".getBytes(),"1002".getBytes(),"bb".getBytes());
//        redisTemplate.opsForHash().put("shunfeng".getBytes(),"1002".getBytes(),"dd".getBytes());
//        redisTemplate.opsForHash().put("shunfeng".getBytes(),"1004".getBytes(),"cc".getBytes());




        byte[] bytes = redisTemplate.dump("shunfeng".getBytes());


        redisTemplate.restore("mykey",bytes,1000,TimeUnit.SECONDS);



        JSON.parseObject(new String(bytes), new TypeReference<Map<String, String>>() {});

        System.out.println();



    }


    public  byte[] serialize(final Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (Exception e) {

        }
        return null;
    }

    public  Object unserialize(final byte[] bytes) {
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {

        }
        return null;
    }

    public  Map<Object, Object> unserializehmbb2moo(final Map<byte[], byte[]> hash) {
        Map<Object, Object> result = new HashMap<Object, Object>();
        try {
            Set<byte[]> keys = hash.keySet();
            if (keys != null && keys.size() > 0) {
                for (byte[] key : keys) {
                    result.put(unserialize(key), unserialize(hash.get(key)));
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

}
