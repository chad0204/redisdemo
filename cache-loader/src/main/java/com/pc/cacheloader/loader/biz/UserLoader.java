package com.pc.cacheloader.loader.biz;

import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.constants.ActionType;
import com.pc.cacheloader.constants.TVMsg;
import com.pc.cacheloader.handler.NodeHandlerFactory;
import com.pc.cacheloader.loader.SimpleTemplateLoader;
import com.pc.cacheloader.mapper.UserMapper;
import com.pc.cacheloader.model.UserEntity;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

/**
 *
 * @author dongxie
 * @date 09:56 2020-05-13
 */
public class UserLoader extends SimpleTemplateLoader<UserEntity> {


    @Resource
    public RedisTemplate loaderRedisTemplate;

    @Autowired
    private NodeHandlerFactory nodeHandlerFactory;

    @Resource
    private TransportClient transportClient;

    @Autowired
    private UserMapper userMapper;


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
     * @param drivingRecordCache
     * @return
     */
    @Override
    protected UserEntity supportLoad(UserEntity drivingRecordCache) {

        return new UserEntity(4L,"dd");
    }



}
