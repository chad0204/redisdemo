package com.pc.cacheloader.distruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO
 *
 * @author dongxie
 * @date 16:47 2020-05-14
 */
public class DisruptorMain {

    public static void main(String[] args) {
        //1.创建一个可缓存的线程 提供线程来出发Consumer 的事件处理
        ExecutorService executorService = Executors.newCachedThreadPool();
        //2.创建Event工厂
        EventFactory<LongEvent> eventEventFactory = new LongEventFactory();
        //3.设置ringBuffer大小
        int ringBufferSize = 1024 * 1024;
        //4.创建Disruptor，单生产者模式，消费者等待策略为YieldingWaitStrategy
        Disruptor<LongEvent> disruptor =
                new Disruptor<>(eventEventFactory, ringBufferSize, executorService, ProducerType.SINGLE, new YieldingWaitStrategy());
        //5.注册消费者
        disruptor.handleEventsWith(new LongEventHandler());
        //可以配置多个消费者，一个生产者 默认重复消费，配置分组
        //6.启动Disruptor
        disruptor.start();



        //7.创建RingBuffer容器
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //8.创建生产者
        LongEventProducer producer = new LongEventProducer(ringBuffer);
        //9.指定缓冲区的大小
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        for (int i = 0; i < 100; i++) {
            //10.将i放在第0个位置
            byteBuffer.putLong(0,i);
            producer.onData(byteBuffer);
        }
        disruptor.shutdown();
        executorService.shutdown();
    }
}
