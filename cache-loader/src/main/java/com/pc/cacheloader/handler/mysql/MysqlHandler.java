package com.pc.cacheloader.handler.mysql;

import com.pc.cacheloader.cache.Cache;
import com.pc.cacheloader.mapper.BaseMapper;
import com.pc.cacheloader.model.BaseDO;

/**
 * @param
 */
public abstract class MysqlHandler<T extends BaseDO> implements Cache<T> {

    protected BaseMapper<T> mapper;

    public MysqlHandler(BaseMapper<T> mapper) {
        this.mapper = mapper;
    }
}
