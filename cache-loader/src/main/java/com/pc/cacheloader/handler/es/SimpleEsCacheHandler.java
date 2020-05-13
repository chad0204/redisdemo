package com.pc.cacheloader.handler.es;

import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.model.BaseDO;
import org.elasticsearch.client.transport.TransportClient;
import java.util.List;

public class SimpleEsCacheHandler<T extends BaseDO> extends EsCacheHandler<T> {

    public SimpleEsCacheHandler(TransportClient transportClient) {
        super(transportClient);
    }

    @Override
    public List getCacheObjectList(Object o) {
        return null;
    }

    @Override
    public Boolean storeCacheObject(T t) {
        return null;
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
}
