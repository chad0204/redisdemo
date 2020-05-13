package com.pc.cacheloader.handler;

import com.pc.cacheloader.cache.Cache;
import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.model.BaseDO;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * cache代理类
 * @param <T>
 */
@Slf4j
public class NodeHandlerCacheProxy<T extends BaseDO> extends NodeHandler implements Cache<T> {

    private Cache<T> cache;


    public NodeHandlerCacheProxy(Cache<T> cache) {
        this.cache = cache;
    }


    @Override
    public Boolean invalidData(CacheTask<T> task) {
        return null;
    }

    @Override
    public void clearData() {

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


    /**
     * 判断当前任务是否执行完成
     * @param cacheTask
     * @return
     */
    private Boolean inCurrentNode(CacheTask<T> cacheTask) {
        if ((cacheTask.getExecuteStep() & this.getPosition()) == 0) {
            if (this.getNext() == null) {
                throw new RuntimeException("cache task un finished..");
            } else {
                return false;
            }
        }
        return true;
    }
}
