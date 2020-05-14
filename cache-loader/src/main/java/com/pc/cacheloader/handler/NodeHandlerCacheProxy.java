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

    private Cache<T> cache;//handler


    public NodeHandlerCacheProxy(Cache<T> cache) {
        this.cache = cache;
    }


    @Override
    public Boolean invalidData(CacheTask<T> cacheTask) {
        if (!inCurrentNode(cacheTask)) {
            return ((NodeHandlerCacheProxy) (this.getNext())).invalidData(cacheTask);
        }

        cache.invalidData(cacheTask);
        cacheTask.setExecuteStep(cacheTask.getExecuteStep() << 1);
        if (this.getNext() != null) {
            return ((NodeHandlerCacheProxy) (this.getNext())).invalidData(cacheTask);
        }
        return true;
    }

    @Override
    public void clearData() {
        log.debug("cache {} clear cache", this.cache);
        cache.clearData();
        if (this.getNext() != null) {
            ((NodeHandlerCacheProxy) (this.getNext())).clearData();
        }
    }

    @Override
    public Boolean increaseData(CacheTask<T> cacheTask) {
        if (!inCurrentNode(cacheTask)) {
            return ((NodeHandlerCacheProxy) (this.getNext())).increaseData(cacheTask);
        }

        cache.increaseData(cacheTask);
        cacheTask.setExecuteStep(cacheTask.getExecuteStep() << 1);
        if (this.getNext() != null) {
            return ((NodeHandlerCacheProxy) (this.getNext())).increaseData(cacheTask);
        }
        return true;
    }

    @Override
    public Boolean modifyData(CacheTask<T> cacheTask) {
        if (!inCurrentNode(cacheTask)) {
            return ((NodeHandlerCacheProxy) (this.getNext())).modifyData(cacheTask);
        }
        cache.modifyData(cacheTask);
        cacheTask.setExecuteStep(cacheTask.getExecuteStep() << 1);
        if (this.getNext() != null) {
            return ((NodeHandlerCacheProxy) (this.getNext())).modifyData(cacheTask);
        }
        return true;
    }

    @Override
    public Boolean loadData(CacheTask<T> cacheTask) {
        if (!inCurrentNode(cacheTask)) {
            return ((NodeHandlerCacheProxy) (this.getNext())).loadData(cacheTask);
        }
        if (cacheTask.getHistory() != null) {
            cache.modifyData(cacheTask);
        } else {
            cache.increaseData(cacheTask);
        }
        cacheTask.setExecuteStep(cacheTask.getExecuteStep() << 1);//*2
        if (this.getNext() != null) {
            return ((NodeHandlerCacheProxy) (this.getNext())).loadData(cacheTask);
        }

        return true;
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
