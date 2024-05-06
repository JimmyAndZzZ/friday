package com.jimmy.friday.center.base;

import org.springframework.context.ApplicationContext;

public interface Initialize {

    void init(ApplicationContext applicationContext) throws Exception;

    int sort();
}
