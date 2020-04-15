package com.pc.redistemplatecluster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * 配置定时任务执行线程数
 * @author dongxie
 * @date 11:26 2020-04-14
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskExecutor() {
        return Executors.newScheduledThreadPool(11);//指定线程池大小
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(taskExecutor());
    }
}
