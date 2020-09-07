package com.pc.rocketmqclient.dfire.message.client.util;


import com.alibaba.dubbo.common.utils.StringUtils;

public class Assert {
    public static void isNotNull(Object source, String... message) {
        if (source == null) {
            throw new RuntimeException(message[0]);
        }
    }


    public static void isTrue(boolean b, String message) {
        if (!b) {
            throw new RuntimeException(message);
        }
    }

    public static void isNotBlank(String source, String message) {
        if (StringUtils.isBlank(source)) {
            throw new RuntimeException(message);
        }
    }

}
