package com.pc.cacheloader.distruptor;


import com.lmax.disruptor.EventHandler;

/**
 * 消费者
 *
 * @author dongxie
 * @date 16:44 2020-05-14
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
        System.out.println("消费者："+longEvent.getValue());
    }
}
