package com.jimmy.friday.agent.bytebuddy;

import com.jimmy.friday.agent.base.MethodsAroundAction;
import com.jimmy.friday.agent.bytebuddy.support.InterceptorInstanceLoader;
import com.jimmy.friday.agent.exception.AgentException;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

public class OverrideCallableMethodAdviceInterceptor {

    private MethodsAroundAction aroundAction;

    public OverrideCallableMethodAdviceInterceptor(String classPath, ClassLoader targetClassLoader) throws AgentException {
        try {
            this.aroundAction = InterceptorInstanceLoader.load(classPath, targetClassLoader);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @RuntimeType
    public Object intercept(@This Object obj, @AllArguments Object[] allArguments, @Origin Method method, @Morph OverrideCallable callable) throws Exception {
        EnhancedInstance enhancedInstance = (EnhancedInstance) obj;
        //时间统计开始
        long start = System.nanoTime();
        //获取入参类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        try {
            //前置
            aroundAction.beforeMethod(enhancedInstance, method, parameterTypes, allArguments);
            // 执行原函数
            Object result = callable.call(allArguments);
            //前置
            aroundAction.afterMethod(enhancedInstance, method, parameterTypes, allArguments, result, System.nanoTime() - start);
            return result;
        } catch (Throwable e) {
            aroundAction.handleMethodException(enhancedInstance, method, parameterTypes, allArguments, e, System.nanoTime() - start);
            throw e;
        } finally {
            aroundAction.ultimate(enhancedInstance, method, parameterTypes, allArguments);
        }
    }
}
