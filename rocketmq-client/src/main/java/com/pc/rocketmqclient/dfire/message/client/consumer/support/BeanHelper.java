package com.pc.rocketmqclient.dfire.message.client.consumer.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pengchao
 * @date 14:05 2020-09-04
 */
@Component
public class BeanHelper implements ApplicationContextAware {

    /**
     * Spring bean容器
     */
    private ApplicationContext applicationContext;

    /**
     * 得到模块单元
     *
     * @param <T>      the type parameter
     * @param beanName the model name
     * @param t        the t
     * @return model
     */
    public <T> T getBean(String beanName, Class<T> t) {
        return applicationContext.getBean(beanName, t);
    }


    /**
     * 得到模块单元
     *
     * @param beanName the model name
     * @return model
     */
    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 获取这个类型的所有bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getBeanList(Class<T> clazz) {
        Map<String, T> beanMap = applicationContext.getBeansOfType(clazz);
        if (beanMap == null || beanMap.isEmpty()) {
            return null;
        }
        return new ArrayList<>(beanMap.values());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
