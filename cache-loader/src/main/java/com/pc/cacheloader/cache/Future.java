package com.pc.cacheloader.cache;

public interface Future {
    void success();
    void failed();
    void timeOut();
}
