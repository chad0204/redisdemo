package com.pc.rocketmqclient.dfire.message.client.consumer.support;


import com.pc.rocketmqclient.dfire.message.client.to.AsyncMsg;

/**
 * 消费类接口,实现该类处理具体的业务类型
 * <p>例子: DoBusiness类只处理messageTagA和messageTagB的消息</p>
 * <blockquote><pre>
 * @MessageTag(tag = {"messageTagA","messageTagB"})
 * public class DoBusiness implements ConsumerCallBack {
 *      @Override
 *      public boolean process(AsyncMsgTO msgTO) {
 *          //do business
 *          return false;
 *      }
 * }
 * </pre></blockquote>
 *
 */
public interface ConsumerCallBack {
    /**
     * 业务具体处理方法
     *
     * @return
     */
    boolean process(AsyncMsg msg);
}
