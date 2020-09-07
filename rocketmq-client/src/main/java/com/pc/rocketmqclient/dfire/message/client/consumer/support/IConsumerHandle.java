package com.pc.rocketmqclient.dfire.message.client.consumer.support;

import com.pc.rocketmqclient.dfire.message.client.to.AsyncMsg;

/**
 *
 * @author pengchao
 * @date 13:53 2020-09-04
 */
public interface IConsumerHandle {

    /**
     * tag
     *
     * @return
     */
    String getSubExpression();

    /**
     * 消费
     *
     * @param asyncMsg
     * @return
     */
    boolean consume(AsyncMsg asyncMsg);
}
