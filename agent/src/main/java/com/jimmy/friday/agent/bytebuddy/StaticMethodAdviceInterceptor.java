package com.jimmy.friday.agent.bytebuddy;

import com.jimmy.friday.agent.base.StaticMethodsAroundAction;
import com.jimmy.friday.agent.bytebuddy.support.InterceptorInstanceLoader;
import com.jimmy.friday.agent.exception.AgentException;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class StaticMethodAdviceInterceptor {

    private StaticMethodsAroundAction aroundAction;

    public StaticMethodAdviceInterceptor(String classPath, ClassLoader targetClassLoader) {
        try {
            this.aroundAction = InterceptorInstanceLoader.load(classPath, targetClassLoader);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @RuntimeType
    public Object intercept(@Origin Class<?> clazz, @Origin Method method, @SuperCall Callable<?> callable, @AllArguments Object[] param) throws Exception {
        //时间统计开始
        long start = System.nanoTime();
        //获取入参类型
        Class<?>[] parameterTypes = method.getParameterTypes();

        try {
            //前置
            aroundAction.beforeMethod(clazz, method, parameterTypes, param);
            // 执行原函数
            Object result = callable.call();
            //前置
            aroundAction.afterMethod(clazz, method, parameterTypes, param, result, System.nanoTime() - start);
            return result;
        } catch (Throwable e) {
            aroundAction.handleMethodException(clazz, method, parameterTypes, param, e, System.nanoTime() - start);
            throw e;
        } finally {
            aroundAction.ultimate(clazz, method, parameterTypes, param);
        }
    }
}
