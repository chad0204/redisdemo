package com.pc.cacheloader.distruptor;

import com.lmax.disruptor.RingBuffer;
import java.nio.ByteBuffer;

/**
 * 生产者
 *
 * @author dongxie
 * @date 16:45 2020-05-14
 */
public class LongEventProducer {

    public final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }


    public void onData(ByteBuffer byteBuffer){
        //1.获取ringBuffer的下标位置
        long sequence = ringBuffer.next();
        Long data = null;

        //2.取出ringBuffer中的空位置
        LongEvent longEvent = ringBuffer.get(sequence);
        //3.然后赋值
        data = byteBuffer.getLong(0);
        longEvent.setValue(data);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            System.out.println("生产者准备发送数据");
            //4.发送数据
            ringBuffer.publish(sequence);
        }
    }

}
