package com.pc.cacheloader.runner;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.cache.DepositTask;
import com.pc.cacheloader.model.CacheMsg;
import com.pc.cacheloader.util.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


//简单实现
@Slf4j
public class EventLoopExecutor implements Runnable {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private AsyncChannelProcess asyncChannelProcess;

    private Integer maxProcessCount;

    private KafkaService kafkaService;

    public EventLoopExecutor(Integer maxProcessCount, ApplicationContext applicationContext) {
        this.maxProcessCount = maxProcessCount;//最大线程数
        kafkaService = applicationContext.getBean(KafkaService.class);
    }

    @Override
    public void run() {
        asyncChannelProcess =  AsyncChannelProcess.newBuilder()
                .setAddBlockTimeout(24, TimeUnit.HOURS)
                .setMaxProcessCount(maxProcessCount)
                .setProcess((Function<CacheTask, Boolean>) cacheTask -> {
                    saveImpl(cacheTask);
                    return true;
                })
                .build();
    }

    private void saveImpl(CacheTask cacheTask) {
        Long startTime = System.currentTimeMillis();
        try {
            cacheTask.run();
            cacheTask.success();//回调
        } catch (Exception e) {
            //失败重试
            log.error("cache task run error {}", cacheTask.toString(), e);
            int retryCount = cacheTask.getRetry().incrementAndGet();
            if (retryCount > 2) {
                cacheTask.failed();
                if (cacheTask.getSource() == 0)
                    kafkaService.sendToRetry(JSON.toJSONString(new CacheMsg(CacheMsg.MsgType.USER,
                            JSON.toJSONString(deposit(cacheTask)),
                            cacheTask.getChannel(), null)));
                return;
            }
            this.addHeadTask(cacheTask);
        }
        Long endTime = System.currentTimeMillis();
        if (atomicInteger.incrementAndGet() % 10000 == 1) {
            log.info("cost time = {} queue size = {}", endTime - startTime);
        }
    }

    public Boolean addTask(CacheTask cacheTask) {
        asyncChannelProcess.add(cacheTask, cacheTask.getOrigin().getChannel());
        return true;
    }

    public Boolean addHeadTask(CacheTask cacheTask) {
        try {
            asyncChannelProcess.addFirst(cacheTask, cacheTask.getOrigin().getChannel());
        } catch (IllegalStateException illegalStateException) {
            log.error("addHeadTask failed..", illegalStateException);
        }
        return true;
    }


    private Object deposit(CacheTask cacheTask) {
        DepositTask depositTask = DepositTask.builder()
                .actionType(cacheTask.getActionType())
                .executeStep(cacheTask.getExecuteStep())
                .initTime(cacheTask.getInitTime())
                .loader(cacheTask.getLoader().getClass())
                .history(cacheTask.getHistory())
                .transfer(cacheTask.getTransfer())
                .origin(cacheTask.getOrigin()).build();
        return depositTask;
    }

}
