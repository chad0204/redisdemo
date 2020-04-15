package com.pc.redisjediscluster.jedis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;
import java.util.Collections;
import java.util.UUID;

/**
 *
 * @author pengchao
 * @since 14:33 2019-10-31
 */
@Component
@Slf4j
public class JedisUtil {

    @Autowired
    private JedisCluster jedisCluster;

    //分片
    private static int shardSize = 200;

    public String shardHget(String key, String field) {
        String result = null;
        try {
            int shard = Math.abs(field.hashCode() % shardSize);
            StringBuilder keySb = new StringBuilder(key);
            keySb.append(":").append(shard);
            result = jedisCluster.hget(keySb.toString(), field);
        } catch (Exception var6) {
            log.error("shard hash set fail, error:", var6);
        }

        return result;
    }


    public boolean shardHset(String key, String field, String value) {
        boolean result = true;

        try {
            int shard = Math.abs(field.hashCode() % shardSize);
            StringBuilder keySb = new StringBuilder(key);
            keySb.append(":").append(shard);
            jedisCluster.hset(keySb.toString(), field, value);
        } catch (Exception var7) {
            result = false;
            log.error("shard hash set fail, error:", var7);
        }

        return result;
    }


    public boolean set(String key,String value) {
        boolean result = true;

        try {
            jedisCluster.set(key,value);
        } catch (Exception var7) {
            result = false;
            log.error("shard hash set fail, error:", var7);
        }

        return result;
    }

    public String get(String key) {
        String result;

        try {
            result = jedisCluster.get(key);
        } catch (Exception var7) {
            result = null;
            log.error("shard hash set fail, error:", var7);
        }

        return result;
    }


    /**
     *
     * @param key
     * @param value UUID.randomUUID().toString()
     * @param expire 过期时间
     * @return
     */
    public Boolean lock(String key,String value,int expire) {
        //set(key, uuid, "NX", "EX", expire)
        if(!StringUtils.isEmpty(jedisCluster.set(key,value,new SetParams().nx().ex(expire)))){
            return true;
        }
        return false;
    }

    private static final Long RELEASE_SUCCESS = 1L;
    public boolean release(String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";//lua
        Object result = jedisCluster.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        return RELEASE_SUCCESS.equals(result);
    }


}
