package com.pc.cacheloader.loader.biz;

import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.constants.ActionType;
import com.pc.cacheloader.constants.TVMsg;
import com.pc.cacheloader.handler.NodeHandlerFactory;
import com.pc.cacheloader.handler.es.EsCacheHandler;
import com.pc.cacheloader.handler.es.SimpleEsCacheHandler;
import com.pc.cacheloader.handler.mysql.SimpleMysqlHandler;
import com.pc.cacheloader.handler.redis.RedisCacheHandler;
import com.pc.cacheloader.handler.redis.SimpleRedisCacheHandler;
import com.pc.cacheloader.loader.SimpleTemplateLoader;
import com.pc.cacheloader.mapper.UserMapper;
import com.pc.cacheloader.model.UserEntity;
import com.pc.cacheloader.util.ScheduleLoad;
import com.pc.cacheloader.util.ScheduleRiseLoad;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 *
 * @author dongxie
 * @date 09:56 2020-05-13
 */
@Component
@Slf4j
@ScheduleLoad
@ScheduleRiseLoad
public class UserLoader extends SimpleTemplateLoader<UserEntity> {


    @Resource
    public RedisTemplate loaderRedisTemplate;

    @Autowired
    private NodeHandlerFactory nodeHandlerFactory;

    @Resource
    private TransportClient transportClient;

    @Autowired
    private UserMapper userMapper;

    public final static String REDIS_KEY = "DATA_LOADER_DR";


    @PostConstruct
    public void runnerInit() {
        redisFunction = new Function<RedisTemplate, RedisCacheHandler<UserEntity>>() {
            @Override
            public RedisCacheHandler<UserEntity> apply(RedisTemplate loaderRedisTemplate) {
                return new SimpleRedisCacheHandler<UserEntity>(loaderRedisTemplate, REDIS_KEY) {

                    @Override
                    public Boolean invalidData(CacheTask<UserEntity> cacheTask) {
                        UserEntity UserEntity = cacheTask.getOrigin();
                        return this.invaildCacheObject(String.valueOf(UserEntity.getId()), UserEntity);
                    }

                    @Override
                    public Boolean increaseData(CacheTask<UserEntity> cacheTask) {
                        UserEntity UserEntity = cacheTask.getOrigin();
                        return this.storeCacheObject(String.valueOf(UserEntity.getId()), UserEntity);
                    }

                    @Override
                    public Boolean modifyData(CacheTask<UserEntity> cacheTask) {
                        UserEntity UserEntity = cacheTask.getOrigin();
                        UserEntity history = this.getCacheObject(String.valueOf(UserEntity.getId()));
                        if (history == null) {
                            throw new IllegalArgumentException("更新数据不存在");
                        }
                        //直接使用新值覆盖旧制 包括null
                        cacheTask.setTransfer(UserEntity);
                        return this.storeCacheObject(String.valueOf(UserEntity.getId()), UserEntity);
                    }
                };

            }

        };

        esFunction = new Function<TransportClient, EsCacheHandler<UserEntity>>() {
            @Override
            public EsCacheHandler<UserEntity> apply(TransportClient transportClient) {
                return new SimpleEsCacheHandler<UserEntity>(transportClient) {

                    @Override
                    public Boolean invalidData(CacheTask<UserEntity> task) {
                        return super.invalidData(task);
                    }

                    @Override
                    public Boolean increaseData(CacheTask<UserEntity> cacheTask) {
                        UserEntity UserEntity = cacheTask.getOrigin();
                        this.createIndexResponse("ids", "drivingRecord", objToEsJson(UserEntity),
                                String.valueOf(UserEntity.getId()));
                        return true;
                    }

                    @Override
                    public Boolean modifyData(CacheTask<UserEntity> cacheTask) {
                        if (cacheTask.getTransfer() != null) {
                            this.updateIndex("ids", "drivingRecord", objToEsJson(cacheTask.getTransfer()),
                                    String.valueOf(cacheTask.getOrigin().getId()));
                            return true;
                        } else {
                            log.error("threadLocal is null.. todo");
                        }
                        return false;
                    }

                    @Override
                    public String objToEsJson(UserEntity o) {
                        return o.toString();
                    }
                };
            }
        };

        /**
         *
         */
        mysqlFunction = mapper -> new SimpleMysqlHandler<UserEntity>(mapper) {
            @Override
            public Boolean modifyData(CacheTask<UserEntity> task) {
                UserEntity userEntity = new UserEntity();
                BeanUtils.copyProperties(task.getOrigin(),userEntity);
                return userMapper.updateById(userEntity) == 1;
            }
        };
    }


    @Override
    public void initHandle() {
        redisCacheHandle = redisFunction.apply(loaderRedisTemplate);
        esCacheHandle = esFunction.apply(transportClient);
        mysqlHandle = mysqlFunction.apply(userMapper);
        this.addLastIncreaseHandle(newNode(redisCacheHandle, nodeHandlerFactory));
        this.addLastIncreaseHandle(newNode(esCacheHandle, nodeHandlerFactory));
        this.addLastLoadHandle(newNode(redisCacheHandle, nodeHandlerFactory));
        this.addLastLoadHandle(newNode(esCacheHandle, nodeHandlerFactory));
        this.addLastModifyHandle(newNode(redisCacheHandle, nodeHandlerFactory));
        this.addLastModifyHandle(newNode(esCacheHandle, nodeHandlerFactory));
        this.addLastModifyHandle(newNode(mysqlHandle, nodeHandlerFactory));
    }

    @Override
    public Boolean riseDataLoad(LocalDateTime start, LocalDateTime end) {
        List<UserEntity> list = Arrays.asList(new UserEntity(3L,"cc"));
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        for(UserEntity user :list) {
            CacheTask<UserEntity> cacheTask = new CacheTask<>(user, ActionType.LOAD, this);
            UserEntity history = new UserEntity(3L,"ccc");
            cacheTask.setHistory(history);
            this.eventLoopExecutor.addTask(cacheTask);
        }
        return true;
    }

    @Override
    public Boolean normalDataLoad() {
        List<UserEntity> list = Arrays.asList(new UserEntity(1L,"aa"),new UserEntity(2L,"bb"));
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        for(UserEntity user :list) {
            this.eventLoopExecutor.addTask(new CacheTask<>(user, ActionType.LOAD, this));
        }
        return true;
    }

    @Override
    public void initCache() {

    }

    /**
     * 拉取最新的数据
     * @param UserEntity
     * @return
     */
    @Override
    protected UserEntity supportLoad(UserEntity UserEntity) {

        return new UserEntity(4L,"dd");
    }



}
