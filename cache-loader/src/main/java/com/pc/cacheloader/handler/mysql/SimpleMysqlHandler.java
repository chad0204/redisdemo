package com.pc.cacheloader.handler.mysql;

import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.mapper.BaseMapper;
import com.pc.cacheloader.model.BaseDO;


public class SimpleMysqlHandler<T extends BaseDO> extends MysqlHandler<T> {

    public SimpleMysqlHandler(BaseMapper<T> mapper) {
        super(mapper);
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
