//package com.pc.redistemplatecluster.jedis;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.stereotype.Component;
//import redis.clients.jedis.JedisCluster;
//
///**
// *
// * @author pengchao
// * @since 14:33 2019-10-31
// */
//@Component
//@Slf4j
//public class JedisUtil {
//
//    @Autowired
//    private JedisCluster jedisCluster;
//
//    //分片
//    private static int shardSize = 200;
//
//    public String shardHget(String key, String field) {
//        String result = null;
//        try {
//            int shard = Math.abs(field.hashCode() % shardSize);
//            StringBuilder keySb = new StringBuilder(key);
//            keySb.append(":").append(shard);
//            result = jedisCluster.hget(keySb.toString(), field);
//        } catch (Exception var6) {
//            log.error("shard hash set fail, error:", var6);
//        }
//
//        return result;
//    }
//
//
//    public boolean shardHset(String key, String field, String value) {
//        boolean result = true;
//
//        try {
//            int shard = Math.abs(field.hashCode() % shardSize);
//            StringBuilder keySb = new StringBuilder(key);
//            keySb.append(":").append(shard);
//            jedisCluster.hset(keySb.toString(), field, value);
//        } catch (Exception var7) {
//            result = false;
//            log.error("shard hash set fail, error:", var7);
//        }
//
//        return result;
//    }
//
//
//    public boolean set(String key,String value) {
//        boolean result = true;
//
//        try {
//            jedisCluster.set(key,value);
//        } catch (Exception var7) {
//            result = false;
//            log.error("shard hash set fail, error:", var7);
//        }
//
//        return result;
//    }
//
//    public boolean get(String key) {
//        boolean result = true;
//
//        try {
//            jedisCluster.get(key);
//        } catch (Exception var7) {
//            result = false;
//            log.error("shard hash set fail, error:", var7);
//        }
//
//        return result;
//    }
//
//
//}
