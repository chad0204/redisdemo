package com.pc.rocketmqclient.dfire.notify;

import com.pc.rocketmqclient.dfire.message.client.consumer.support.annotation.MessageTag;
import com.pc.rocketmqclient.dfire.model.SimpleMsg;

/**
 *
 * @author pengchao
 * @date 11:11 2020-09-04
 */
@MessageTag(tag = {"tagA","tagB"})
public class SimpleCallback extends AbstractNotifyCallBack<SimpleMsg> {

    /**
     * 子类处理具体业务逻辑
     *
     * @param msg
     * @return
     */
    @Override
    protected boolean _process(SimpleMsg msg) {

        System.out.println("simpleCallback收到消息："+msg.getMsgContent()+",msgId="+msg.getMsgId());
        return true;
    }
}
