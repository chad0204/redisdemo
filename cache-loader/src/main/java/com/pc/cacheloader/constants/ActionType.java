package com.pc.cacheloader.constants;

/**
 * 操作类型
 */
public enum ActionType {
    LOAD(1),
    INCREASE(2),
    MODIFY(3),
    INVALID(4);

    Integer value;

    ActionType(Integer value) {
        this.value = value;
    }
}