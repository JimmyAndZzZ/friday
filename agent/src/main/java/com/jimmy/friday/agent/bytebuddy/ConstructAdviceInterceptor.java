package com.jimmy.friday.agent.bytebuddy;

import com.jimmy.friday.agent.base.ConstructInterceptorAction;
import com.jimmy.friday.agent.bytebuddy.support.InterceptorInstanceLoader;
import com.jimmy.friday.agent.exception.AgentException;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public class ConstructAdviceInterceptor {

    private ConstructInterceptorAction constructInterceptorAction;

    public ConstructAdviceInterceptor(String classPath, ClassLoader targetClassLoader) {
        try {
            this.constructInterceptorAction = InterceptorInstanceLoader.load(classPath, targetClassLoader);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @RuntimeType
    public void intercept(@This Object obj, @AllArguments Object[] param) throws Throwable {
        EnhancedInstance enhancedInstance = (EnhancedInstance) obj;
        constructInterceptorAction.onConstruct(enhancedInstance, param);
    }
}
