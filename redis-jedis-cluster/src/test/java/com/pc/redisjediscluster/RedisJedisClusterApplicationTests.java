package com.pc.redisjediscluster;

import com.pc.redisjediscluster.jedis.JedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisJedisClusterApplicationTests {

    @Autowired
    private JedisUtil jedisUtil;

    @Test
    public void contextLoads() {


        jedisUtil.set("ddd","111");

        System.out.println(jedisUtil.get("ddd"));

    }

}
