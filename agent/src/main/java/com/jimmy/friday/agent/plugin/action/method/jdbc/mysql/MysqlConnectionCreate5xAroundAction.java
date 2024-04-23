package com.jimmy.friday.agent.plugin.action.method.jdbc.mysql;

import com.jimmy.friday.agent.base.StaticMethodsAroundAction;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.other.jdbc.ConnectionCache;
import com.jimmy.friday.agent.other.jdbc.ConnectionInfo;
import com.jimmy.friday.agent.plugin.action.method.jdbc.parse.URLParser;

import java.lang.reflect.Method;
import java.util.Objects;

public class MysqlConnectionCreate5xAroundAction implements StaticMethodsAroundAction {

    @Override
    public void beforeMethod(Class clazz, Method method, Class<?>[] parameterTypes, Object[] param) {

    }

    @Override
    public void afterMethod(Class clazz, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        if (result instanceof EnhancedInstance) {
            String database = Objects.isNull(param[3]) ? "" : param[3].toString();
            ConnectionInfo connectionInfo = ConnectionCache.get(param[0].toString(), param[1].toString(), database);
            if (connectionInfo == null) {
                connectionInfo = URLParser.parser(param[4].toString());
            }

            EnhancedField enhancedField = new EnhancedField();
            enhancedField.setDynamic(connectionInfo);
            ((EnhancedInstance) result).setDynamicField(enhancedField);
        }
    }

    @Override
    public void handleMethodException(Class clazz, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
