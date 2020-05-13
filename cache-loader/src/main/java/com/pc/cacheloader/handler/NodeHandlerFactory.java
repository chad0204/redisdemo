package com.pc.cacheloader.handler;

import com.pc.cacheloader.cache.Cache;
import com.pc.cacheloader.model.BaseDO;
import org.springframework.stereotype.Component;

@Component
public class NodeHandlerFactory {

    /**
     *
     * @param cache
     * @param <T>
     * @return
     */
    public <T extends BaseDO> NodeHandlerCacheProxy<T> createHandle(Cache<T> cache) {
        return new NodeHandlerCacheProxy<>(cache);
    }
}
