package com.jimmy.friday.agent.plugin.action.method.jdbc.mysql;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.other.jdbc.ConnectionInfo;
import com.jimmy.friday.agent.other.jdbc.StatementEnhanceInfos;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;

import java.lang.reflect.Method;

public class MysqlCreatePreparedStatementAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {

    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        if (result instanceof EnhancedInstance) {
            EnhancedField enhancedField = new EnhancedField();
            enhancedField.setDynamic(new StatementEnhanceInfos((ConnectionInfo) enhancedInstance.getDynamicField().getDynamic(), (String) param[0], "PreparedStatement"));
            ((EnhancedInstance) result).setDynamicField(enhancedField);
        }
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
