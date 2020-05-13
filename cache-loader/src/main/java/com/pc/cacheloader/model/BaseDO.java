package com.pc.cacheloader.model;

import java.io.Serializable;

/**
 *
 */
public abstract class BaseDO implements Serializable {
    //用于任务hash取模
    public abstract Long getChannel();
}
