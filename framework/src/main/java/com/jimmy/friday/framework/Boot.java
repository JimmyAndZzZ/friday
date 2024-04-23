package com.jimmy.friday.framework;

import com.jimmy.friday.framework.base.Bootstrap;
import com.jimmy.friday.framework.support.TransmitSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Map;


public class Boot implements InitializingBean, ApplicationContextAware, BeanPostProcessor {

    private static ApplicationContext applicationContext;

    private TransmitSupport transmitSupport;

    public Boot(TransmitSupport transmitSupport) {
        this.transmitSupport = transmitSupport;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Bootstrap> beansOfType = applicationContext.getBeansOfType(Bootstrap.class);
        Collection<Bootstrap> values = beansOfType.values();

        for (Bootstrap value : values) {
            value.bootstrapBefore();
        }

        transmitSupport.init();

        for (Bootstrap value : values) {
            value.bootstrapAfter();
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Map<String, Bootstrap> beansOfType = applicationContext.getBeansOfType(Bootstrap.class);
        Collection<Bootstrap> values = beansOfType.values();

        for (Bootstrap value : values) {
            bean = value.beanProcess(bean, beanName);
        }

        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Boot.applicationContext = applicationContext;
    }

    static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
