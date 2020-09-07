package com.pc.rocketmqclient.dfire.model;


/**
 * @author pengchao
 * @date 13:43 2020-09-04
 */
public class SimpleMsg extends BaseMsg {

    private String msgContent;


    public SimpleMsg(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }





}
