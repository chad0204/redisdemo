package com.pc.rocketmqclient.dfire.model;

import java.io.Serializable;

/**
 * @author pengchao
 * @date 13:43 2020-09-04
 */
public abstract class BaseMsg implements Serializable {

    private String msgId;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
