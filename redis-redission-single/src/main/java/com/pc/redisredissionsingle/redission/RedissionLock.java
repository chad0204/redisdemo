package com.pc.redisredissionsingle.redission;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author dongxie
 * @date 17:08 2020-04-03
 */
@Component
@Slf4j
public class RedissionLock {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * @param lockName
     * @return
     */
    public boolean getLock(String lockName) {
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean tryLock = lock.tryLock(1, 15, TimeUnit.SECONDS);
            return tryLock;
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("getLock error , msg :{}", e.getMessage());
            return false;
        }
    }

    public boolean getLockAndWait(String lockName, int waitTime){
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean tryLock = lock.tryLock(waitTime, 15, TimeUnit.SECONDS);
            return tryLock;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解锁
     *
     * @param lockKey
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock != null) {
            lock.unlock();
        }
    }
}
