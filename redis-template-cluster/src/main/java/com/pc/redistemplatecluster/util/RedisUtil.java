package com.pc.redistemplatecluster.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author pengchao
 * @since 18:05 2019-10-18
 */

/**
 *
 *  基于spring和redis的redisTemplate工具类 针对所有的hash 都是以h开头的方法 针对所有的Set 都是以s开头的方法 不含通用方法 针对所有的List 都是以l开头的方法
 */

/**
 * SpringBoot自动帮我们在容器中生成了一个RedisTemplate和一个StringRedisTemplate
 */
@Component
@Slf4j
public class RedisUtil {


    /**
     *  使用Springboot自动配置的redis,RedisProperties读取配置文件中的配置,RedisAutoConfiguration配置了bean
     */
    @Autowired
    private StringRedisTemplate redisTemplate;//这里注入的其实是springboot提供的StringRedisTemplate


    private static int shardSize = 200;



    //================分片====================================

    public Object shardHget(String key, String field) {
        Object result = null;
        try {
            int shard = Math.abs(field.hashCode() % shardSize);
            StringBuilder keySb = new StringBuilder(key);
            keySb.append(":").append(shard);
            result = this.hget(keySb.toString(), field);
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
            this.hset(keySb.toString(), field, value);
        } catch (Exception var7) {
            result = false;
            log.error("shard hash set fail, error:", var7);
        }

        return result;
    }



    //============================lock===============================


    private static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }


    /**
     *
     * 方式一：
     * 加锁和设置超时时间非原子性，导致死锁
     * 谁都可以解锁
     * @param lock
     * @param time
     * @param unit
     * @return
     */
    public boolean tryDistributeLock(String lock,long time,TimeUnit unit) {
        try {
            if(redisTemplate.opsForValue().setIfAbsent(lock,"lock")) {
//                int a = 1/0;
                redisTemplate.expire(lock,time,unit);//程序在此崩溃，未执行，将死锁，当然如果删除lock可以解决，但是除非客户端拿到锁，不然也不会执行解锁。
                return true;
            }
        } catch (Exception e) {
            log.error("redis error");
        }
        return false;
    }

    /**
     * 这种不先判断锁的拥有者而直接解锁的方式，会导致任何客户端都可以随时进行解锁，即使这把锁不是它的
     * @param lock
     * @return
     */
    public boolean release(String lock) {
        try {
            this.del(lock);
            return true;
        } catch (Exception var3) {
            log.warn("release lock fail, error:", var3);
            return false;
        }
    }

    /**
     *  方式二：
     *
     *  1.key是锁，value是锁的过期时间，通过value来判断过期，所以不会死锁。
     *  2.如果锁已经存在则获取锁的过期时间，和当前时间比较，如果锁已经过期，则设置新的过期时间，返回加锁成功
     *  3.如果锁被其他客户端删除，则立即重试获取锁，并设置重试超时时间
     *
     *
     *  缺陷：
     *  1.由于是客户端自己生成过期时间，所以需要强制要求分布式下每个客户端的时间必须同步。
     *  2.当锁过期的时候，如果多个客户端同时执行getset()方法，那么虽然最终只有一个客户端可以加锁，但是这个客户端的锁的过期时间可能被其他客户端覆盖。
     *  3.锁不具备拥有者标识，即任何客户端都可以解锁
     *
     *
     * @param lock 锁名
     * @param lockExpireTime 过期时间
     * @param requestTimeout 超时时间
     * @return
     */
    public boolean tryDistributeLock(String lock, long lockExpireTime, long requestTimeout) {
        if(StringUtils.isEmpty(lock)) {
            throw new IllegalArgumentException("lock invalid");
        }
        if(lockExpireTime <= 0) {
            throw new IllegalArgumentException("lockExpireTime invalid");
        }
        if(requestTimeout <= 0) {
            throw new IllegalArgumentException("requestTimeout invalid");
        }
        //注意：自旋等锁，如果超时时间比获得锁的客户端的执行时间长，释放的时候该客户端会获取到锁（所以如果是控制定时任务只有一个执行那超时时间没必要，同时客户端也没必要删除锁。
        // 当需要多个客户端调度执行，控制资源访问时，需要重试和删除锁）
        //当然如果是一天执行一次的业务，可以将expireTime设置的较长一些
        while (requestTimeout > 0) {
            String expire = String.valueOf(System.currentTimeMillis() + lockExpireTime + 1);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lock, expire);
            if (result) {
                //目前没有客户端占用此锁
                return true;
            }
            Object currentValue = redisTemplate.opsForValue().get(lock);
            if (currentValue == null) {
                //锁已经被其他客户端删除，可以考虑马上重试获取锁，还是依然返回false。
//                continue;
                //如果只控制一个客户端执行，那么既然其他客户端执行，则无需执行
                return false;
            } else if (Long.parseLong(String.valueOf(currentValue)) < System.currentTimeMillis()) {
                //此处判断出锁已经超过了其有效的存活时间
                Object oldValue = redisTemplate.opsForValue().getAndSet(lock, expire);
                //1.如果拿到的旧值是空则说明在此客户端做getSet之前已经有客户端将锁删除，由于此客户端getSet操作之后已经对锁设置了值，实际上相当于它已经占有了锁
                //2.如果拿到的旧值不为空且等于前面查到的值，则说明在此客户端进行getSet操作之前没有其他客户端对锁设置了值,则此客户端是第一个占有锁的
                return oldValue == null || oldValue.equals(currentValue);
            }
            long sleepTime;
            if (requestTimeout > 100) {//默认睡眠时间
                sleepTime = 100;
                requestTimeout -= 100;
            } else {
                sleepTime = requestTimeout;
                requestTimeout = 0;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RedisSystemException("redis lock error",e);
            }
        }
        return false;
    }

    /**
     * 释放分布式锁
     * 也是所有客户端都可以解锁
     *
     * @param lockKey 锁
     * @return 是否释放成功
     */
    public boolean releaseDistributedLock(String lockKey) {
        String value = redisTemplate.opsForValue().get(lockKey);
        if (value != null && Long.parseLong(value) > System.currentTimeMillis()) {
            //如果锁还存在并且还在有效时间则进行删除
            redisTemplate.delete(lockKey);
        }
        return true;
    }


    /**
     *
     *  方式三:多参数的set
     *
     *    第一个为key，我们使用key来当锁，因为key是唯一的。
     *    第二个为value，我们传的是requestId，很多童鞋可能不明白，有key作为锁不就够了吗，为什么还要用到value？原因就是我们在上面讲到可靠性时，分布式锁要满足第四个条件解铃还须系铃人，通过给value赋值为requestId，我们就知道这把锁是哪个请求加的了，在解锁的时候就可以有依据。requestId可以使用UUID.randomUUID().toString()方法生成。
     *    第三个为nxxx，这个参数我们填的是NX，意思是SET IF NOT EXIST，即当key不存在时，我们进行set操作；若key已经存在，则不做任何操作；
     *    第四个为expx，这个参数我们传的是PX，意思是我们要给这个key加一个过期的设置，具体时间由第五个参数决定。
     *    第五个为time，与第四个参数相呼应，代表key的过期时间。
     *
     *
     *    nx 只在键不存在时,才对键进行设置操作,       SET key value NX === SETNX key value
     *    xx 只在键已经存在时， 才对键进行设置操作。   SET key value XX(无SETXX命令)
     *    ex 将键的过期时间设置为 seconds 秒,        SET key value EX seconds === SETEX key seconds value
     *    px 将键的过期时间设置为 milliseconds 毫秒, SET key value PX milliseconds === PSETEX key milliseconds value
     *
     *    加锁：
     *    set key value ex seconds nx
     *
     *
     * @param key
     * @param expire String uuid = ;
     * @return
     */
    /**
     *
     * @param key
     * @param value UUID.randomUUID().toString()
     * @param expire
     * @return
     */
    public boolean tryLock(String key,String value, long expire, long requestTimeout) {
        //获取锁
        RedisCallback<String> callback = (connection) -> {
            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            return commands.set(key, value, "NX", "EX", expire);//过期时间别太短
        };
        if(!StringUtils.isEmpty(redisTemplate.execute(callback))) {
            return true;
        }

        //第一次获取锁失败，开始重试
        while (requestTimeout > 0) {
            try {
                if(!StringUtils.isEmpty(redisTemplate.execute(callback))) {
                    return true;
                }
            } catch (Exception e) {
                log.error("set redis occured an exception", e);
            }

            if(StringUtils.isEmpty(getLock(key))) {//说明被释放，赶紧重试
                continue;
            }

            long sleepTime;
            if (requestTimeout > 100) {//默认睡眠时间
                sleepTime = 100;
                requestTimeout -= 100;
            } else {
                sleepTime = requestTimeout;
                requestTimeout = 0;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RedisSystemException("redis lock error",e);
            }
        }
        return false;
    }

    public boolean releaseLock(String key,String requestId) {
        // 释放锁的时候，有可能因为持锁之后方法执行时间大于锁的有效期，此时有可能已经被另外一个线程持有锁，所以不能直接删除
        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(requestId);

            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            // spring自带的执行脚本方法中，集群模式直接抛出不支持执行脚本的异常，所以只能拿到原redis的connection来执行脚本
            RedisCallback<Long> callback = (connection) -> {
                Object nativeConnection = connection.getNativeConnection();
                // 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                // 集群模式
                if (nativeConnection instanceof JedisCluster) {
                    return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, keys, args);
                }
                // 单机模式
                else if (nativeConnection instanceof Jedis) {
                    return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, args);
                }
                return 0L;
            };
            Long result = redisTemplate.execute(callback);

            return result != null && result > 0;
        } catch (Exception e) {
            log.error("release lock occured an exception", e);
        } finally {
            // 清除掉ThreadLocal中的数据，避免内存溢出
//            lockFlag.remove();
        }
        return false;
    }

    public String getLock(String key) {
        try {
            RedisCallback<String> callback = (connection) -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                return commands.get(key);
            };
            return redisTemplate.execute(callback);
        } catch (Exception e) {
            log.error("get redis occured an exception", e);
        }
        return "";
    }



    /**
     * 获取信号量
     *
     *  限制一项资源同时能被多少进程访问
     *  信号量也是一种锁，只是锁未获取会阻塞，等待锁释放，而信号量未获取一般会直接退出，由用户处理失败
     *
     * @param sem_name
     * @param limit
     * @param timeout
     * @return
     */
    public synchronized String acquireSemaphore(String sem_name,int limit,long timeout) {
        String identifier= UUID.randomUUID().toString();
        Long nowTime = System.currentTimeMillis();
        //按照系统时间清除过期信号量
        redisTemplate.opsForZSet().removeRangeByScore(sem_name,0,nowTime-timeout);

        redisTemplate.opsForZSet().add(sem_name, identifier, nowTime);//不管排名，先将标识添加进去

        Long rank = redisTemplate.opsForZSet().rank(sem_name, identifier);//获取改标识的分值的排名


        if(rank !=null && rank<limit){
            System.out.println(Thread.currentThread().getName()+"  identifier rank :"+rank);
            return identifier;
        }else{
            System.out.println(Thread.currentThread().getName()+"  identifier rank :"+rank+",too late");
        }

        redisTemplate.opsForZSet().remove(sem_name, identifier);//获取失败，删掉之前add的标识
        return null;
    }

    public Long releaseSemaphore(String sem_name,String identifier){
        return redisTemplate.opsForZSet().remove(sem_name, identifier);
    }


    // =============================common============================


    /**
     * 指定缓存失效时间
     *
     * @param key
     *            键
     * @param time
     *            时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key
     *            键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key
     *            键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key
     *            可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    // ============================String=============================
    /**
     * 普通缓存获取
     *
     * @param key
     *            键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key
     *            键
     * @param value
     *            值
     * @return true成功 false失败
     */
    public boolean set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }

    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key
     *            键
     * @param value
     *            值
     * @param time
     *            时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, String value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 递增 适用场景： https://blog.csdn.net/y_y_y_k_k_k_k/article/details/79218254 高并发生成订单号，秒杀类的业务逻辑等。
     *
     * @param key
     *            键
     *            要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key
     *            键
     *            要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        //mq.opsForValue().decrement(key,delta);
//        mq.opsForValue().increment(key);
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Map=================================
    /**
     * HashGet
     *
     * @param key
     *            键 不能为null
     * @param item
     *            项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key
     *            键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key
     *            键
     * @param map
     *            对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key
     *            键
     * @param map
     *            对应多个键值
     * @param time
     *            时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key
     *            键
     * @param item
     *            项
     * @param value
     *            值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key
     *            键
     * @param item
     *            项
     * @param value
     *            值
     * @param time
     *            时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key
     *            键 不能为null
     * @param item
     *            项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key
     *            键 不能为null
     * @param item
     *            项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key
     *            键
     * @param item
     *            项
     * @param by
     *            要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key
     *            键
     * @param item
     *            项
     * @param by
     *            要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================set=============================
    /**
     * 根据key获取Set中的所有值
     *
     * @param key
     *            键
     * @return
     */
    public Set<String> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(key, e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key
     *            键
     * @param value
     *            值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key
     *            键
     * @param values
     *            值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, String... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key
     *            键
     * @param time
     *            时间(秒)
     * @param values
     *            值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, String... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count;
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key
     *            键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key
     *            键
     * @param values
     *            值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, String... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    // ============================zset=============================
    /**
     * 根据key获取Set中的所有值
     *
     * @param key
     *            键
     * @return
     */
    public Set<String> zSGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(key, e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key
     *            键
     * @param value
     *            值
     * @return true 存在 false不存在
     */
    public boolean zSHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    public Boolean zSSet(String key, String value, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, value, 2);
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key
     *            键
     * @param time
     *            时间(秒)
     * @param values
     *            值 可以是多个
     * @return 成功个数
     */
    public long zSSetAndTime(String key, long time, String... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count;
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key
     *            键
     * @return
     */
    public long zSGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key
     *            键
     * @param values
     *            值 可以是多个
     * @return 移除的个数
     */
    public long zSetRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @取出来的元素 总数 end-start+1
     *
     * @param key
     *            键
     * @param start
     *            开始 0 是第一个元素
     * @param end
     *            结束 -1代表所有值
     * @return
     */
    public List<String> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error(key, e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key
     *            键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key
     *            键
     * @param index
     *            索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error(key, e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key
     *            键
     * @param value
     *            值
     *            时间(秒)
     * @return
     */
    public boolean lSet(String key, String value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key
     *            键
     * @param value
     *            值
     * @param time
     *            时间(秒)
     * @return
     */
    public boolean lSet(String key, String value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key
     *            键
     * @param value
     *            值
     *            时间(秒)
     * @return
     */
    public boolean lSet(String key, List<String> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key
     *            键
     * @param value
     *            值
     * @param time
     *            时间(秒)
     * @return
     */
    public boolean lSet(String key, List<String> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key
     *            键
     * @param index
     *            索引
     * @param value
     *            值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, String value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error(key, e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key
     *            键
     * @param count
     *            移除多少个
     * @param value
     *            值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }


}



