package com.pc.rocketmqclient.dfire.message.client.annotation;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User:jiandan
 * Date:2016/3/25.
 * Time:10:52.
 * INFO:使用这个注解，表示这个属性是非常重要的
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
public @interface ImportantField {
}
