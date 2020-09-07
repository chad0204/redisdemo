package com.pc.rocketmqclient.dfire.message.client.consumer.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 *
 * @author pengchao
 * @date 11:17 2020-09-04
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageTag {
    /**
     * 配置多个Tag,配置方法查看 {@link com.twodfire.async.message.client.consumer.support.ConsumerCallBack}
     *
     * @return
     */
    String[] tag() default {};
}
