package com.jimmy.friday.agent.base;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;

public interface ConstructInterceptorAction {

    void onConstruct(EnhancedInstance enhancedInstance, Object[] allArguments) throws Throwable;

    void setClassName(String className);
}
