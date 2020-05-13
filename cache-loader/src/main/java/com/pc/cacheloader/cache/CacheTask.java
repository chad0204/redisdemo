package com.pc.cacheloader.cache;


import com.pc.cacheloader.constants.ActionType;
import com.pc.cacheloader.loader.AbstractLoader;
import com.pc.cacheloader.model.BaseDO;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 缓存操作
 * @param <T>
 */

@Data
public class CacheTask<T extends BaseDO> extends BaseDO implements Future, Runnable {

    private LocalDateTime initTime; //任务初始化时间

    ActionType actionType; //任务类型，增删改查

    private T origin;   //原始数据
    private T transfer; //更新数据
    private T history;  //历史缓存


    private Integer executeStep = 1; //成功步数

    private AbstractLoader<T> loader; //加载器

    private Function<Boolean,T> callBack; //回调函数，用于控制外部队列的任务同步

    private AtomicInteger retry = new AtomicInteger(0); //重试次数

    private Integer source = 0;//1表示DepositTask ，0表示CacheTask，判断重试


    public CacheTask() {
    }

    /**
     * 有回调函数
     * @param origin
     * @param actionType
     * @param loader
     * @param function 回调函数
     */
    public CacheTask(T origin, ActionType actionType, AbstractLoader<T> loader, Function function) {
        this.origin = origin;
        this.actionType = actionType;
        this.loader = loader;
        initTime = LocalDateTime.now();
        callBack = function;
    }

    /**
     *  无回调函数
     * @param origin
     * @param actionType
     * @param loader
     */
    public CacheTask(T origin, ActionType actionType, AbstractLoader<T> loader) {
        this(origin, actionType, loader, null);
    }

    //参数构建器
    public static Builder newBuilder(){
        return new Builder();
    }
    public final static class Builder<T extends BaseDO> {

        private CacheTask<T> cacheTask;

        private Builder() {
            cacheTask = new CacheTask();
        }

        private Builder(CacheTask cacheTask) {
            this.cacheTask = cacheTask;
        }

        public Builder origin(T t) {
            cacheTask.setOrigin(t);
            return this;
        }

        public Builder history(T t) {
            cacheTask.setHistory(t);
            return this;
        }

        public Builder transfer(T t) {
            cacheTask.setTransfer(t);
            return this;
        }

        public Builder source(Integer t) {
            cacheTask.setSource(t);
            return this;
        }

        public Builder actionType(ActionType t) {
            cacheTask.setActionType(t);
            return this;
        }

        public Builder initTime(LocalDateTime t) {
            cacheTask.setInitTime(t);
            return this;
        }

        public Builder executeStep(Integer t) {
            cacheTask.setExecuteStep(t);
            return this;
        }

        public Builder loader(AbstractLoader t) {
            cacheTask.setLoader(t);
            return this;
        }

        public CacheTask<T> build(){
            return cacheTask;
        }
    }


    @Override
    public void success() {
        if (this.callBack != null)
            this.callBack.apply(true);
    }

    @Override
    public void failed() {
        if (this.callBack != null)
            this.callBack.apply(false);
    }

    @Override
    public void timeOut() {

    }

    @Override
    public Long getChannel() {
        return origin.getChannel();
    }


    @Override
    public void run() {
        switch (actionType) {
            case LOAD:
                loader.loadMsg(this);
                break;
            case INCREASE:
                loader.increaseMsg(this);
                break;
            case MODIFY:
                loader.modifyMsg(this);
                break;
            case INVALID:
                loader.invalidMsg(this);
                break;
            default:
                throw new RuntimeException("unknown action type");
        }
    }


}


