package com.jimmy.friday.agent.plugin.action.method;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.other.RunMonitorPool;
import com.jimmy.friday.agent.utils.JsonUtil;
import com.jimmy.friday.boot.core.agent.RunLine;
import com.jimmy.friday.boot.enums.agent.CommandTypeEnum;

import java.lang.reflect.Method;
import java.util.BitSet;


public class CmdMethodsAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        BitSet match = RunMonitorPool.match(enhancedInstance.getClass().getName(), method.getName());

        EnhancedField enhancedField = enhancedInstance.getDynamicField();
        boolean isNeedMonitor = match.get(0);
        boolean isNeedWatch = match.get(1);
        if (isNeedMonitor || isNeedWatch) {
            if (enhancedField == null) {
                enhancedField = new EnhancedField();
            }

            enhancedField.setIsNeedMonitor(isNeedMonitor);
            enhancedField.setIsNeedWatch(isNeedWatch);
            enhancedInstance.setDynamicField(enhancedField);
        }

        if (enhancedField != null && Boolean.TRUE.equals(enhancedField.getIsNeedMonitor())) {
            enhancedField.getMonitorCount().incrementAndGet();
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        RunLine build = RunLine.builder()
                .method(method.getName())
                .clazz(enhancedInstance.getClass().getName())
                .param(this.paramSerialize(param))
                .returnValue(result)
                .cost(cost)
                .build();

        RunMonitorPool.process(build);
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {
        RunLine build = RunLine.builder()
                .method(method.getName())
                .clazz(enhancedInstance.getClass().getName())
                .param(this.paramSerialize(param))
                .isException(true)
                .cost(cost)
                .build();

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement errorLine = stackTrace[0];
            int lineNumber = errorLine.getLineNumber();
            build.setExceptionLineCount(lineNumber);
        }

        RunMonitorPool.process(build);
    }

    @Override
    public void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        EnhancedField dynamicField = enhancedInstance.getDynamicField();

        if (dynamicField != null) {
            Boolean isNeedWatch = dynamicField.getIsNeedWatch();
            Boolean isNeedMonitor = dynamicField.getIsNeedMonitor();

            if (isNeedWatch) {
                RunMonitorPool.finish(CommandTypeEnum.WATCH);
            }

            if (isNeedMonitor) {
                int i = dynamicField.getMonitorCount().decrementAndGet();
                if (i == 0) {
                    RunMonitorPool.finish(CommandTypeEnum.TRACE);
                }
            }
        }
    }

    protected String paramSerialize(Object[] param) {
        if (param == null || param.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Object o : param) {
            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(JsonUtil.toString(o));
        }

        return sb.toString();
    }
}
