package com.pc.cacheloader.loader;

import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.constants.ActionType;
import com.pc.cacheloader.constants.SinkModel;
import com.pc.cacheloader.constants.TVMsg;
import com.pc.cacheloader.handler.es.EsCacheHandler;
import com.pc.cacheloader.handler.mysql.MysqlHandler;
import com.pc.cacheloader.handler.redis.RedisCacheHandler;
import com.pc.cacheloader.mapper.BaseMapper;
import com.pc.cacheloader.model.BaseDO;
import com.pc.cacheloader.runner.EventLoopExecutor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 *
 * @author dongxie
 * @date 09:52 2020-05-13
 */
@Slf4j
public abstract class SimpleTemplateLoader<T extends BaseDO> extends AbstractLoader<T> {

    protected EventLoopExecutor eventLoopExecutor;

    protected Function<RedisTemplate, RedisCacheHandler<T>> redisFunction;

    protected Function<TransportClient, EsCacheHandler<T>> esFunction;

    protected Function<BaseMapper, MysqlHandler<T>> mysqlFunction;

    protected RedisCacheHandler<T> redisCacheHandle;

    protected EsCacheHandler<T> esCacheHandle;

    protected MysqlHandler<T> mysqlHandle;


    @Override
    public AbstractLoader<T> registerExecutor(EventLoopExecutor eventLoopExecutor) {
        this.eventLoopExecutor = eventLoopExecutor;
        return this;
    }


    @Override
    public Boolean consumerMsg(TVMsg msg) {
        try {
            if (msg.getIsSync()) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                AtomicInteger atomicInteger = new AtomicInteger(0);
                CacheTask cacheTask = null;

                if (msg.getT() instanceof CacheTask) {
                    cacheTask = (CacheTask) msg.getT();
                } else if (msg.getSinkModel().equals(SinkModel.CREATE)) {
                    cacheTask = gmtCacheTask(atomicInteger, countDownLatch, (T) msg.getT(), ActionType.INCREASE,
                            this);
                } else if (msg.getSinkModel().equals(SinkModel.MODIFIED)) {
                    cacheTask = gmtCacheTask(atomicInteger, countDownLatch, (T) msg.getT(), ActionType.MODIFY,
                            this);
                } else if (msg.getSinkModel().equals(SinkModel.LOAD)) {
                    cacheTask = gmtCacheTask(atomicInteger, countDownLatch, supportLoad((T) msg.getT()),
                            ActionType.MODIFY,
                            this);
                } else {
                    throw new IllegalArgumentException("un support sink model");
                }
                eventLoopExecutor.addTask(cacheTask);
                countDownLatch.await();//阻塞到任务执行完
                return atomicInteger.get() == 1;
            } else {
                if (msg.getT() instanceof CacheTask) {
                    return eventLoopExecutor.addTask((CacheTask) msg.getT());
                } else if (msg.getSinkModel().equals(SinkModel.CREATE)) {
                    return eventLoopExecutor.addTask(new CacheTask<>((T) msg.getT(), ActionType.INCREASE, this));
                } else if (msg.getSinkModel().equals(SinkModel.MODIFIED)) {
                    return eventLoopExecutor.addTask(new CacheTask<>((T) msg.getT(), ActionType.MODIFY, this));
                } else if (msg.getSinkModel().equals(SinkModel.LOAD)) {
                    return eventLoopExecutor.addTask(new CacheTask<>((supportLoad((T) msg.getT())), ActionType.LOAD, this));
                } else {
                    throw new IllegalArgumentException("un support sink model");
                }
            }
        } catch (Exception e) {
            log.error("modify failed..", e);
        }
        return false;
    }

    /**
     * 提供额外重写补充loader方法
     *
     */
    protected T supportLoad(T t) {
        return t;
    }


    private CacheTask gmtCacheTask(AtomicInteger atomicInteger,
                                     CountDownLatch countDownLatch,
                                     T t,
                                     ActionType increase,
                                     AbstractLoader<T> abstractLoader) {
        //封装task
        return new CacheTask<>(t, increase, abstractLoader,
                //全局队列中task执行完后的回调函数，只有全局的分同步异步，loader私用的只有异步
                (Function<Boolean, Object>) result -> {
                    if (result == true) {
                        atomicInteger.incrementAndGet();
                    }
                    countDownLatch.countDown();
                    return null;
                });
    }



}
