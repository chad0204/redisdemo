package com.pc.rocketmqclient.dfire.message.client.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author gantang
 * @Date 2017/3/21
 */
public class MsgUtils {
    public static final String MESSAGE_TRACE = "message_trace";

    public final static <T> boolean isAllNull(T... objs) {
        for (T t : objs) {
            if (t != null) return false;
        }
        return true;
    }

    public static String getOriginMsgId(MessageExt messageExt) {
        if (messageExt.getReconsumeTimes() > 0) {
            String msgId = messageExt.getProperty("ORIGIN_MESSAGE_ID");
            if (StringUtils.isNotBlank(msgId)) {
                return msgId;
            }
        }
        return messageExt.getMsgId();
    }
}
