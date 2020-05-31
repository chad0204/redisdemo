package com.pc.cacheloader.distruptor;

import com.lmax.disruptor.EventFactory;

/**
 * TODO
 *
 * @author dongxie
 * @date 16:48 2020-05-14
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
