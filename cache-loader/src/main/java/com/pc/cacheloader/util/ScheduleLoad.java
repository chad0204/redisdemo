package com.pc.cacheloader.util;

import java.lang.annotation.*;


@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
//注解需要定时全量的loader
public @interface ScheduleLoad {

}
