package com.pc.cacheloader.cache;


import com.pc.cacheloader.model.BaseDO;
import java.util.List;

/**
 * 二级缓存（es mongo的存储）
 */
public interface Level2Cache<T extends BaseDO> extends Cache<T>{

    /**
     *
     * @param o
     * @return
     */
    List<T> getCacheObjectList(Object o);

    /**
     *
     * @param t
     * @return
     */
    Boolean storeCacheObject(T t);

}
