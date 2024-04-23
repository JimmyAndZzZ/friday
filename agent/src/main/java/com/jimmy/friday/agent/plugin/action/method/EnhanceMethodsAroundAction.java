package com.jimmy.friday.agent.plugin.action.method;

import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.AnnotationLoad;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.support.PointSupport;
import com.jimmy.friday.agent.support.TraceSupport;

import java.lang.reflect.Method;
import java.util.Date;


public class EnhanceMethodsAroundAction extends CmdMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        Context context = ContextHold.getContext();
        if (context == null) {
            context = new Context();
            ContextHold.setContext(context);

            EnhancedField enhancedField = new EnhancedField();
            enhancedField.setIsNeedTrace(true);
            enhancedInstance.setDynamicField(enhancedField);
        }

        String methodName = method.getName();
        String className = enhancedInstance.getClass().getName();

        if (ConfigLoad.getDefault().logPointIsMatch(className, methodName) || AnnotationLoad.traceIsMatch(className, methodName)) {
            context.setClassPoint(className);
            context.setMethodPoint(methodName);
            context.setIsNeedPushLog(true);

            PointSupport.getInstance().send(new Date(), className, methodName, context.getTraceId());
        }

        if (ContextHold.getQpsFlagHolder() != null && (ConfigLoad.getDefault().qpsPointIsMatch(className, methodName) || AnnotationLoad.qpsIsMatch(className, methodName))) {
            ContextHold.setQpsFlagHolder(true);
        }

        EnhancedField dynamicField = enhancedInstance.getDynamicField();
        if (dynamicField != null && Boolean.TRUE.equals(dynamicField.getIsNeedTrace())) {
            dynamicField.getTraceCount().incrementAndGet();
        }

        super.beforeMethod(enhancedInstance, method, parameterTypes, param);
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        super.afterMethod(enhancedInstance, method, parameterTypes, param, result, cost);
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {
        Context context = ContextHold.getContext();
        if (context != null && context.getIsNeedPushLog()) {
            TraceSupport.getDefault().send(Trace.builder()
                    .date(new Date())
                    .level("ERROR")
                    .logMessage("运行失败")
                    .className(enhancedInstance.getClass().getName())
                    .methodName(method.getName())
                    .param(super.paramSerialize(param))
                    .spanId(context.getSpanId())
                    .traceId(context.getTraceId())
                    .isLog(false).build());
        }

        super.handleMethodException(enhancedInstance, method, parameterTypes, param, throwable, cost);
    }

    @Override
    public void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        //清除上下文
        EnhancedField dynamicField = enhancedInstance.getDynamicField();
        if (dynamicField != null && Boolean.TRUE.equals(dynamicField.getIsNeedTrace())) {
            //防止递归
            int i = dynamicField.getTraceCount().decrementAndGet();
            if (i == 0) {
                ContextHold.removeContext();
            }
        }

        String methodName = method.getName();
        String className = enhancedInstance.getClass().getName();

        Context context = ContextHold.getContext();
        if (context != null) {
            if (Boolean.TRUE.equals(context.getIsNeedPushLog()) && className.equalsIgnoreCase(context.getClassPoint()) && methodName.equalsIgnoreCase(context.getMethodPoint())) {
                context.setIsNeedPushLog(false);
            }
        }

        super.ultimate(enhancedInstance, method, parameterTypes, param);
    }
}
