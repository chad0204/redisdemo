package com.pc.zkclient;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * TODO
 *
 * @author dongxie
 * @date 09:48 2020-05-08
 */
@Component
public class Test implements InitializingBean, FactoryBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet");
    }


    @PostConstruct
    public void init() {
        System.out.println("postConstruct");
    }


    @Override
    public Object getObject() throws Exception {

        System.out.println("getBean");

        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }
}
