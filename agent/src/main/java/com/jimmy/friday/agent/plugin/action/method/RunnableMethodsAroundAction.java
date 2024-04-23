package com.jimmy.friday.agent.plugin.action.method;

import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;

import java.lang.reflect.Method;


public class RunnableMethodsAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        EnhancedField enhancedField = enhancedInstance.getDynamicField();
        if (enhancedField != null) {
            Context context = ContextHold.getContext();
            if (context == null) {
                context = new Context();
                context.setTraceId(enhancedField.getDynamic().toString());

                Boolean isNeedPushLog = enhancedField.getAttachment("isNeedPushLog", Boolean.class);
                if (isNeedPushLog != null) {
                    context.setIsNeedPushLog(isNeedPushLog);
                }

                ContextHold.setContext(context);
            }
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {

    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }

    @Override
    public void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        //清除上下文
        Object dynamicField = enhancedInstance.getDynamicField();
        if (dynamicField != null) {
            ContextHold.removeContext();
        }
    }
}
