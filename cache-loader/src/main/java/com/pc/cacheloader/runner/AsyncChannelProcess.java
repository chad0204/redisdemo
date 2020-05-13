package com.pc.cacheloader.runner;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class AsyncChannelProcess<T> {
    private int maxProcessCount;
    private int maxBufferSizePreProcess;
    private Function<T, Boolean> process;
    private Function<List<T>, Boolean> batchProcess;
    private Adder<T> adder;

    //队列号，task队列
    private Map<Long, LinkedBlockingDeque<T>> mapQueue = new HashMap<>();

    public static <P> Builder<P> newBuilder() {
        return new Builder<>();
    }


    public static class Builder<T> {
        private int maxProcessCount = 10;
        private int maxBufferSizePreProcess = 1000;
        private Function<T, Boolean> process;
        private Function<List<T>, Boolean> batchProcess;
        private Blocking adder;
        private Long timeout;
        private TimeUnit unit;


        private Builder() {
        }

        public Builder setMaxProcessCount(int maxProcessCount) {
            this.maxProcessCount = maxProcessCount;
            return this;
        }

        public Builder setMaxBufferSizePreProcess(int maxBufferSizePreProcess) {
            this.maxBufferSizePreProcess = maxBufferSizePreProcess;
            return this;
        }

        public Builder setProcess(Function<T, Boolean> process) {
            this.process = process;
            return this;
        }

        public Builder setBatchProcess(Function<List<T>, Boolean> batchProcess) {
            this.batchProcess = batchProcess;
            return this;
        }

        public Builder setAdder(Adder<T> adder) {
            this.adder = (Blocking) adder;
            return this;
        }

        public Builder setAddBlockTimeout(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
            return this;
        }

        public AsyncChannelProcess<T> build() {
            Preconditions.checkState(this.process != null || this.batchProcess != null, "process can not be null");

            AsyncChannelProcess<T> asyncChannelProcess = new AsyncChannelProcess<T>();
            if (this.adder == null) {
                if (timeout != null && unit != null) {
                    this.adder = new Blocking<T>(timeout, unit);
                } else {
                    this.adder = new Blocking();
                }
            }

            asyncChannelProcess.process = this.process;
            asyncChannelProcess.batchProcess = this.batchProcess;
            asyncChannelProcess.adder = this.adder;
            asyncChannelProcess.maxBufferSizePreProcess = this.maxBufferSizePreProcess;
            asyncChannelProcess.maxProcessCount = this.maxProcessCount;
            if (this.process != null) {
                asyncChannelProcess.start();
            } else if (this.batchProcess != null) {
                asyncChannelProcess.startBatch();
            }
            return asyncChannelProcess;
        }

    }


    private interface Adder<T> {
        boolean add(LinkedBlockingDeque queue, T t);

        boolean addFirst(LinkedBlockingDeque queue, T t);
    }

    public class DropOldest<T> implements Adder<T> {
        @Override
        public boolean add(LinkedBlockingDeque queue, T t) {
            if (queue.size() >= maxBufferSizePreProcess) {
                queue.remove();
            }
            return queue.add(t);
        }

        @Override
        public boolean addFirst(LinkedBlockingDeque queue, T t) {
            try {
                queue.addFirst(t);
                return true;
            } catch (IllegalStateException e) {
                log.error("", e);
                return false;
            }
        }

    }

    public static class Blocking<T> implements Adder<T> {
        private long timeout;
        private TimeUnit unit;

        public Blocking(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
        }

        public Blocking() {

        }

        @Override
        public boolean add(LinkedBlockingDeque queue, T t) {
            try {
                return queue.offer(t, timeout, unit);
            } catch (InterruptedException e) {
                log.error("", e);
            }
            return false;
        }

        @Override
        public boolean addFirst(LinkedBlockingDeque queue, T t) {
            try {
                queue.addFirst(t);
                return true;
            } catch (IllegalStateException illegalStateException) {
                log.error("", illegalStateException);
                return false;
            }
        }
    }


    public void add(T t, Long channel) {
        long m = Math.abs(channel % maxProcessCount);
        LinkedBlockingDeque queue = mapQueue.get(m);
        adder.add(queue, t);
    }

    public void addFirst(T t, Long channel) {
        long m = Math.abs(channel % maxProcessCount);
        LinkedBlockingDeque queue = mapQueue.get(m);
        adder.addFirst(queue, t);
    }


    private void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(maxProcessCount);
        for (long i = 0; i < maxProcessCount; i++) {
            final LinkedBlockingDeque<T> queue = new LinkedBlockingDeque<>(maxBufferSizePreProcess);
            mapQueue.put(i, queue);
            executorService.submit(new Runnable() {
                private LinkedBlockingDeque<T> blockingQueue = queue;

                @Override
                public void run() {
                    while (true) {
                        try {
                            T t = blockingQueue.take();
                            process.apply(t);//执行缓存任务
                        } catch (Throwable e) {
                            log.error("", e);
                        }
                    }
                }
            });

        }
    }

    private void startBatch() {
        ExecutorService executorService = Executors.newFixedThreadPool(maxProcessCount);

        for (long i = 0; i < maxProcessCount; i++) {
            final LinkedBlockingDeque<T> queue = new LinkedBlockingDeque(maxBufferSizePreProcess);
            mapQueue.put(i, queue);
            executorService.submit(new Runnable() {
                private LinkedBlockingDeque<T> blockingQueue = queue;
                private LinkedList<T> batch = new LinkedList<>();

                @Override
                public void run() {
                    while (true) {
                        try {
                            int size = blockingQueue.size();
                            if (size > 0) {
                                for (int i = 0; i < size; i++) {
                                    batch.add(blockingQueue.take());
                                }
                                batchProcess.apply(batch);
                                batch.clear();
                            } else {
                                batch.add(blockingQueue.take());
                                batchProcess.apply(batch);
                                batch.clear();
                            }
                        } catch (Throwable e) {
                            log.error("", e);
                        }
                    }
                }
            });

        }
    }
}
