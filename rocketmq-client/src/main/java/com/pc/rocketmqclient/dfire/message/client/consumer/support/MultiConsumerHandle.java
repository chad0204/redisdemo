package com.pc.rocketmqclient.dfire.message.client.consumer.support;

/**
 * TODO
 *
 * @author pengchao
 * @date 11:20 2020-09-04
 */

import com.pc.rocketmqclient.dfire.message.client.consumer.support.annotation.MessageTag;
import com.pc.rocketmqclient.dfire.message.client.to.AsyncMsg;
import io.opentracing.ActiveSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

/**
 * 同Topic的多个Consumer可以一起设置,减少XML的配置(目前是ONS)消息
 * * <p>Xml配置方法</p>
 * <blockquote><pre>
 * <bean id="businessMultiConsumerHandle" class="com.twodfire.async.message.client.consumer.support.MultiConsumerHandle">
 *      <property name="callbackList">
 *          <list>
 *              //多个实现{@link ConsumerCallBack}的bean配置好,一个ConsumerListener就能分别处理同Topic同一个Consumer多个Tag的消息
 *              <bean class="com.dfire.notify.XXXXBusiness"/>
 *              <bean class="com.dfire.notify.YYYYBusiness"/>
 *          </list>
 *      </property>
 *      <property name="topic" value="${ons.topic}"/>
 * </bean>
 * </pre></blockquote>
 *
 * @author pengchao
 * @date 11:20 2020-09-04
 * @see ConsumerCallBack
 */
public class MultiConsumerHandle extends ConsumerHandle {

    private Logger log = LoggerFactory.getLogger(MultiConsumerHandle.class);

    /**
     * 如果一个ConsumerCallBack对应多个tag,那么就缓存多份
     */
    private Map<String/* tag */, ConsumerCallBack> callbackCaches = new ConcurrentHashMap<String, ConsumerCallBack>();

    @Autowired
    BeanHelper beanHelper;


    /**
     * 多个Tag分隔符,ONS默认为"||"
     */
    private String tagSplit = "||";


    public MultiConsumerHandle(List<ConsumerCallBack> callbackList) {

        if (callbackList == null || callbackList.isEmpty()) {
            return;
        }
        StringBuffer _subExpression = new StringBuffer();
        for (ConsumerCallBack consume : callbackList) {
            if (consume == null) {
                continue;
            }
            Class<?> clazz=consume.getClass();
            if(isProxyBean(consume)){
                clazz = AopUtils.getTargetClass(consume);
            }
            MessageTag messageTag = clazz.getAnnotation(MessageTag.class);
            if (messageTag == null) {
                log.error("配置ConsumeCallback的messageTag为空,请检查后配置:" + consume);
            }
            String[] tags = messageTag.tag();
            if (tags == null || tags.length == 0) {
                log.error("配置的messageTag没有添加具体类型的tag,请检查后配置(使用方法:@MessagetTag(tag={\"xxx\",\"yyy\"})):" + consume);
            }
            for (String tag : tags) {
                callbackCaches.put(tag, consume);
                _subExpression.append(tag).append(tagSplit);
            }
        }
        String subExpression = _subExpression.toString();
        if (subExpression.endsWith(tagSplit)) {
            subExpression = subExpression.substring(0, subExpression.length() - tagSplit.length());
        }
        log.debug("已订阅:[" + subExpression + "]这些类型的消息");
        //只接收配置了相应Tag的消息
        setSubExpression(subExpression);

    }

    /**
     * 处理消息Handle，业务逻辑在此处理
     *
     * @param message
     * @return true:处理成功;false:处理失败，重新投递
     */
    @Override
    public boolean consume(AsyncMsg message) {
        boolean result = true;
        try {
            ConsumerCallBack consume = callbackCaches.get(message.getTag());
            //没有相应tag的消费处理类,不应该被接收处理到的,可能出现异常投递过来的.
            if (consume == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Tag[" + message.getTag() + "]该类型对应的的ConsumeCallback不存在,不处理,如要处理,请配置在callbackList中.");
                }
                return result;
            }
            result = consume.process(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result = false;
        }
        return result;
    }



    private boolean isProxyBean(Object bean) {
        return AopUtils.isAopProxy(bean);
    }

}
