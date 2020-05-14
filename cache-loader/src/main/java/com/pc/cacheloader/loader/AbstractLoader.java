package com.pc.cacheloader.loader;

import com.pc.cacheloader.cache.Cache;
import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.constants.ActionType;
import com.pc.cacheloader.constants.TVMsg;
import com.pc.cacheloader.handler.NodeHandlerCacheProxy;
import com.pc.cacheloader.handler.NodeHandlerChain;
import com.pc.cacheloader.handler.NodeHandlerFactory;
import com.pc.cacheloader.model.BaseDO;
import com.pc.cacheloader.runner.EventLoopExecutor;
import com.pc.cacheloader.runner.LoaderInitialize;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据加载器
 *
 * @author dongxie
 * @date 13:59 2020-05-11
 */
@Slf4j
public abstract class AbstractLoader<T extends BaseDO> implements LoaderInitialize {
    //按照操作类型分类的handler链
    protected Map<ActionType, NodeHandlerChain<T>> map;//每个handlerChain中包含redisHandler，esHandler..


    public NodeHandlerChain<T> nodeIncreaseHandles;

    public NodeHandlerChain<T> nodeModifyHandles;

    public NodeHandlerChain<T> nodeLoadHandles;

    @Getter
    protected Class aClass = null;

    /**
     * 初始化loader时，初始化handler链容器map
     */
    public AbstractLoader() {
        nodeIncreaseHandles = new NodeHandlerChain<>(this, ActionType.INCREASE);
        nodeModifyHandles = new NodeHandlerChain<>(this, ActionType.MODIFY, ActionType.INVALID);
        nodeLoadHandles = new NodeHandlerChain<>(this, ActionType.LOAD);
        map = initMap(nodeIncreaseHandles, nodeModifyHandles, nodeLoadHandles);
    }

    private Map<ActionType, NodeHandlerChain<T>> initMap(NodeHandlerChain<T> nodeIncreaseHandles,
                                                         NodeHandlerChain<T> nodeModifyHandles,
                                                         NodeHandlerChain<T> nodeLoadHandles) {
        Map<ActionType, NodeHandlerChain<T>> map = new HashMap<>();
        chainToMap(map, nodeIncreaseHandles);
        chainToMap(map, nodeModifyHandles);
        chainToMap(map, nodeLoadHandles);
        return map;
    }

    private void chainToMap(Map<ActionType, NodeHandlerChain<T>> map, NodeHandlerChain<T> nodeHandleChain) {
        if (nodeHandleChain != null && nodeHandleChain.getActionTypes() != null) {
            for (ActionType actionType : nodeHandleChain.getActionTypes()) {
                map.put(actionType, nodeHandleChain);
            }
        }
    }

    protected NodeHandlerCacheProxy<T> newNode(Cache<T> cache, NodeHandlerFactory nodeHandleFactory) {
        return nodeHandleFactory.createHandle(cache);
    }


    //添加新增数据handler
    public void addLastIncreaseHandle(NodeHandlerCacheProxy<T> nodeHandle) {
        nodeIncreaseHandles.addLastNodeHandler(nodeHandle);
    }
    //添加加载数据handler
    public void addLastLoadHandle(NodeHandlerCacheProxy<T> nodeHandle) {
        nodeLoadHandles.addLastNodeHandler(nodeHandle);
    }

    //添加更新数据handler
    public void addLastModifyHandle(NodeHandlerCacheProxy<T> nodeHandle) {
        nodeModifyHandles.addLastNodeHandler(nodeHandle);
    }


    //初始化事件驱动器
    public abstract AbstractLoader<T> registerExecutor(EventLoopExecutor eventLoopExecutor);

    //全局执行器
    public abstract Boolean consumerMsg(TVMsg msg);

    //初始化handler链
    public abstract void initHandle();


    //增量更新
    public Boolean riseDataLoad(LocalDateTime start, LocalDateTime end) {
        return null;
    }

    //全量更新
    public Boolean normalDataLoad() {
        return null;
    }



    /**
     * 获取头节点的handler更新数据
     * @param
     */
    public Boolean modifyMsg(CacheTask<T> t) {
        return map.get(ActionType.MODIFY).getHead().modifyData(t);
    }

    /**
     * 获取头节点的handler清除数据
     * @param
     */
    public Boolean invalidMsg(CacheTask<T> t) {
        return map.get(ActionType.INVALID).getHead().invalidData(t);
    }

    /**
     * 获取头节点的handler加载数据
     * @param
     */
    public Boolean loadMsg(CacheTask<T> t) {
        return map.get(ActionType.LOAD).getHead().loadData(t);
    }

    /**
     *获取头节点的handler新增数据
     */
    public Boolean increaseMsg(CacheTask<T> t) {
        return map.get(ActionType.INCREASE).getHead().increaseData(t);
    }



    public Class supportType() {
        if (aClass == null) {
            //获取loader泛型类型，也就是数据
            Type tp = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            aClass = (Class<T>) tp;
            return aClass;
        }
        return aClass;
    }





}
