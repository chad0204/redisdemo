package com.pc.rocketmqclient.dfire.notify;

import com.pc.rocketmqclient.dfire.message.client.consumer.support.ConsumerCallBack;
import com.pc.rocketmqclient.dfire.message.client.to.AsyncMsg;
import com.pc.rocketmqclient.dfire.model.BaseMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.ParameterizedType;

/**
 * callBack基类
 *
 * @author pengchao
 * @date 11:23 2020-09-04
 */
public abstract class AbstractNotifyCallBack<T extends BaseMsg> implements ConsumerCallBack {
    /**
     * 交易消息日志
     */
    private static Logger logger = LoggerFactory.getLogger(ConsumerCallBack.class);


    private Class<T> clazz;

    /**
     * 当前子类的泛型class
     */
    protected AbstractNotifyCallBack() {
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        clazz = (Class<T>) pt.getActualTypeArguments()[0];
    }

    @Override
    public boolean process(AsyncMsg msg) {
        long startTime = System.currentTimeMillis();
        boolean result;
        //消息内容为空
        if (msg == null) {
            return true;
        }
        String logTitle = "处理" + msg.getTag() + "_Notify消息";
        //转换出错
        Object obj = msg.getContent();
        if (obj == null) {
            logger.error("消息内容为空:", msg);
            return true;
        }
        T content;
        if (clazz.isAssignableFrom(obj.getClass())) {
            content = (T) obj;
            content.setMsgId(msg.getMsgID());
        } else {
            logger.error(logTitle + "类型转换失败", "msg_class=" + obj.getClass() + ",t_class=" + clazz);
            return true;
        }
        //是否需要打印日志
        logger.info(logTitle + "开始", "msgId=" + msg.getMsgID() + ",Msg=" + msg);
        try {
            result = _process(content);
        } catch (Exception e) {
            result = false;
            logger.info(logTitle + "失败!", content);
        }
        //消息处理时间统计
        logger.info(logTitle + "处理速度"+
                "msg_id"+msg.getMsgID()+
                "result"+result+
                "cost_time"+ (System.currentTimeMillis() - startTime));
        //是否需要打印日志
        logger.info(logTitle + "结束", "msgId=" + msg.getMsgID() + ",处理结果=" + result + ",Msg=" + msg);
        //成功||最大重试时间||最大重试次数
        return result || maxReconsumerTimes(msg, content) || maxReconsumerCount(msg, content);
    }

    /**
     * 超出最大重试次数
     *
     * @param msg
     * @return
     */
    private boolean maxReconsumerCount(AsyncMsg msg, T content) {
        long maxRetryCnt = getMaxRetryCnt(content);
        if (maxRetryCnt > 0) {
            int retryCnt = msg.getReconsumeTimes() + 1;
            if (retryCnt >= maxRetryCnt) {
//                NOTIFY_LOGGER.warn(LogUtil.formatLog("重试次数超过最大值"
//                        , "maxRetryCnt=" + maxRetryCnt, ",retryCnt=" + retryCnt));
                callback(msg);
                return true;
            }
        }
        return false;
    }

    /**
     * 超出最大重试次数
     *
     * @param msg
     * @return
     */
    private boolean maxReconsumerTimes(AsyncMsg msg, T content) {
        long maxRetryTime = getMaxRetryTime(content);
        if (maxRetryTime > 0) {
            long retryTime = System.currentTimeMillis() - msg.getStartDeliverTime();
            if (retryTime >= maxRetryTime) {
//                NOTIFY_LOGGER.warn(LogUtil.formatLog("重试时间超过最大值"
//                        , "maxRetryTime=" + maxRetryTime, ",retryTime=" + retryTime));
                callback(msg);
                return true;
            }
        }
        return false;
    }

    //获取消息最大重试次数，默认-1表示不限次数
    protected long getMaxRetryCnt(T content) {
        return -1;
    }

    //获取消息最大重试时间，默认-1表示不限时间
    protected long getMaxRetryTime(T content) {
        return -1;
    }

    //超过最大重试时间或者次数回调
    protected void callback(AsyncMsg msg) {

    }

    /**
     * 子类处理具体业务逻辑
     *
     * @param content
     * @return
     */
    protected abstract boolean _process(T content);
}
