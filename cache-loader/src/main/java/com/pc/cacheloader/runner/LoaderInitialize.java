package com.pc.cacheloader.runner;

/**
 *
 * 实现该接口，启动是loader就需要加载数据到缓存
 * @author dongxie
 * @date 14:03 2020-05-11
 */
public interface LoaderInitialize {

    /**
     * 初始化缓存
     */
    void initCache();
}
