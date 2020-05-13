package com.pc.cacheloader.handler.redis;

import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.model.BaseDO;
import org.springframework.data.redis.core.RedisTemplate;

public class SimpleRedisCacheHandler<T extends BaseDO> extends RedisCacheHandler<T> {

    private String topic;

    public SimpleRedisCacheHandler(RedisTemplate redisTemplate, String topic) {
        super(redisTemplate);
        this.topic = topic;
    }

    public SimpleRedisCacheHandler(RedisTemplate redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public String topic() {
        return topic;
    }


    @Override
    public Boolean invalidData(CacheTask<T> task) {
        return null;
    }

    @Override
    public Boolean increaseData(CacheTask<T> task) {
        return null;
    }

    @Override
    public Boolean modifyData(CacheTask<T> task) {
        return null;
    }

    @Override
    public Boolean loadData(CacheTask<T> task) {
        return null;
    }
}
