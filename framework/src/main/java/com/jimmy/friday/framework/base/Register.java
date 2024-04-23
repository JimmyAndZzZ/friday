package com.jimmy.friday.framework.base;

import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import org.springframework.core.type.filter.TypeFilter;

import java.util.List;

public interface Register {

    ServiceTypeEnum type();

    void register() throws Exception;

    Service getService() throws Exception;

    List<Method> getMethods() throws Exception;

    void collectMethod(Class<?> clazz) throws Exception;

    TypeFilter getTypeFilter();
}
