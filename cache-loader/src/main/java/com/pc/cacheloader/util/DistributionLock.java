package com.pc.cacheloader.util;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;

@Component
public class DistributionLock {
    @Autowired
    @Qualifier("loaderRedisTemplate")
    private StringRedisTemplate basicRedisTemplate;

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;


    /**
     * 默认锁有效时间(单位毫秒)
     */
    long DEFAULT_LOCK_EXPIRE_TIME = 10000;
    /**
     * 默认睡眠时间(单位毫秒)
     */
    long DEFAULT_SLEEP_TIME = 100;

    /**
     * 尝试获取分布式锁
     *
     * @return 是否获取成功
     */
    public boolean tryGetDistributedLock(String lock, long lockExpireTime, long requestTimeout) {
        Preconditions.checkArgument(!StringUtils.isEmpty(lock), "lock invalid");
        Preconditions.checkArgument(lockExpireTime > 0, "lockExpireTime invalid");
        Preconditions.checkArgument(requestTimeout > 0, "requestTimeout invalid");
        try {
            while (requestTimeout > 0) {
                String expire = String.valueOf(System.currentTimeMillis() + lockExpireTime + 1);
                Boolean result = basicRedisTemplate.opsForValue().setIfAbsent(lock, expire);
                if (result) {
                    //目前没有线程占用此锁
                    return true;
                }
                Object currentValue = basicRedisTemplate.opsForValue().get(lock);
                if (currentValue == null) {
                    //锁已经被其他线程删除马上重试获取锁
                    continue;
                } else if (Long.parseLong(String.valueOf(currentValue)) < System.currentTimeMillis()) {
                    //此处判断出锁已经超过了其有效的存活时间
                    Object oldValue = basicRedisTemplate.opsForValue().getAndSet(lock, expire);
                    if (oldValue == null || oldValue.equals(currentValue)) {
                        //1.如果拿到的旧值是空则说明在此线程做getSet之前已经有线程将锁删除，由于此线程getSet操作之后已经对锁设置了值，实际上相当于它已经占有了锁
                        //2.如果拿到的旧值不为空且等于前面查到的值，则说明在此线程进行getSet操作之前没有其他线程对锁设置了值,则此线程是第一个占有锁的
                        return true;
                    }
                }
                long sleepTime = 0;
                if (requestTimeout > DEFAULT_SLEEP_TIME) {
                    sleepTime = DEFAULT_SLEEP_TIME;
                    requestTimeout -= DEFAULT_SLEEP_TIME;
                } else {
                    sleepTime = requestTimeout;
                    requestTimeout = 0;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                } catch (InterruptedException e) {

                }
            }
            return false;
        } finally {

        }
    }

    public boolean tryGetDistributedLock(String lock, long requestTimeout) {
        return this.tryGetDistributedLock(lock, DEFAULT_LOCK_EXPIRE_TIME, requestTimeout);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁
     * @return 是否释放成功
     */
    public boolean releaseDistributedLock(String lockKey) {
        String value = basicRedisTemplate.opsForValue().get(lockKey);
        if (value != null && Long.parseLong(value) > System.currentTimeMillis()) {
            //如果锁还存在并且还在有效时间则进行删除
            basicRedisTemplate.delete(lockKey);
        }
        return true;
    }
}
