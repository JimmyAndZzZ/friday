package com.jimmy.friday.agent.plugin.action.method.jdbc.mysql;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.other.jdbc.ConnectionInfo;
import com.jimmy.friday.agent.other.jdbc.StatementEnhanceInfos;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;

import java.lang.reflect.Method;

public class MysqlCreateStatementAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {

    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        if (result instanceof EnhancedInstance) {
            EnhancedField enhancedInstanceDynamicField = enhancedInstance.getDynamicField();
            if (enhancedInstanceDynamicField != null) {
                EnhancedField enhancedField = new EnhancedField();
                enhancedField.setDynamic(new StatementEnhanceInfos((ConnectionInfo) enhancedInstanceDynamicField.getDynamic(), "Statement"));
                ((EnhancedInstance) result).setDynamicField(enhancedField);
            }
        }
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
