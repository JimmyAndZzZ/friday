package com.jimmy.friday.framework.base;

import org.springframework.beans.BeansException;

public interface Bootstrap {

    void bootstrapBefore() throws Exception;

    Object beanProcess(Object bean, String beanName) throws BeansException;

    void bootstrapAfter() throws Exception;
}
