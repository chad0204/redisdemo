package com.pc.rocketmqclient.dfire.notify;

import com.pc.rocketmqclient.dfire.message.client.consumer.support.annotation.MessageTag;
import com.pc.rocketmqclient.dfire.model.SimpleMsg;
import org.springframework.stereotype.Component;

/**
 *
 * @author pengchao
 * @date 11:11 2020-09-04
 */
@MessageTag(tag = {"tagA","tagC"})
public class Simple1Callback extends AbstractNotifyCallBack<SimpleMsg> {

    /**
     * 子类处理具体业务逻辑
     *
     * @param msg
     * @return
     */
    @Override
    protected boolean _process(SimpleMsg msg) {

        System.out.println("simple1Callback收到消息："+msg.getMsgContent()+",msgId="+msg.getMsgId());
        return true;
    }
}
