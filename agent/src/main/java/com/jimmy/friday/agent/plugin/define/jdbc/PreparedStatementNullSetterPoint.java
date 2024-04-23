package com.jimmy.friday.agent.plugin.define.jdbc;

import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public final class PreparedStatementNullSetterPoint implements MethodsInterceptPoint {

    @Override
    public ElementMatcher<MethodDescription> getMethodsMatcher() {
        return named("setNull");
    }

    @Override
    public String getMethodsAround() {
        return "com.jimmy.friday.agent.plugin.action.method.jdbc.JdbcPreparedStatementNullSetterAction";
    }

    @Override
    public boolean isOverrideArgs() {
        return false;
    }
}
