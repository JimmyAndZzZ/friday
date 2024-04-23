package com.jimmy.friday.agent.plugin.action.method.jdbc;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.other.jdbc.StatementEnhanceInfos;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;

import java.lang.reflect.Method;

public class JdbcPreparedStatementSetterAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        EnhancedField enhancedInstanceDynamicField = enhancedInstance.getDynamicField();
        if (enhancedInstanceDynamicField != null) {
            StatementEnhanceInfos statementEnhanceInfos = (StatementEnhanceInfos) enhancedInstanceDynamicField.getDynamic();

            if (statementEnhanceInfos != null) {
                int index = (Integer) param[0];
                Object parameter = param[1];
                statementEnhanceInfos.setParameter(index, parameter);
            }
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {

    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}