package com.jimmy.friday.agent.bytebuddy;

import com.jimmy.friday.agent.base.MethodsAroundAction;
import com.jimmy.friday.agent.bytebuddy.support.InterceptorInstanceLoader;
import com.jimmy.friday.agent.exception.AgentException;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class MethodAdviceInterceptor {

    private MethodsAroundAction aroundAction;

    public MethodAdviceInterceptor(String classPath, ClassLoader targetClassLoader) {
        try {
            this.aroundAction = InterceptorInstanceLoader.load(classPath, targetClassLoader);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @RuntimeType
    public Object intercept(@This Object obj, @Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object[] param) throws Exception {
        EnhancedInstance enhancedInstance = (EnhancedInstance) obj;
        //时间统计开始
        long start = System.nanoTime();
        //获取入参类型
        Class<?>[] parameterTypes = method.getParameterTypes();

        try {
            //前置
            aroundAction.beforeMethod(enhancedInstance, method, parameterTypes, param);
            // 执行原函数
            Object result = callable.call();
            //前置
            aroundAction.afterMethod(enhancedInstance, method, parameterTypes, param, result, System.nanoTime() - start);
            return result;
        } catch (Throwable e) {
            aroundAction.handleMethodException(enhancedInstance, method, parameterTypes, param, e, System.nanoTime() - start);
            throw e;
        } finally {
            aroundAction.ultimate(enhancedInstance, method, parameterTypes, param);
        }
    }
}
