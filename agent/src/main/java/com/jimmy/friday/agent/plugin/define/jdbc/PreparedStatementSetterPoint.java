package com.jimmy.friday.agent.plugin.define.jdbc;

import com.google.common.collect.Sets;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;

public class PreparedStatementSetterPoint implements MethodsInterceptPoint {

    private static final Set<String> PS_SETTERS = Sets.newHashSet("setArray", "setBigDecimal", "setBoolean", "setByte", "setDate", "setDouble", "setFloat", "setInt", "setLong", "setNString", "setObject", "setRowId", "setShort", "setString", "setTime", "setTimestamp", "setURL");

    private static final Set<String> PS_IGNORABLE_SETTERS = Sets.newHashSet("setAsciiStream", "setBinaryStream", "setBlob", "setBytes", "setCharacterStream", "setClob", "setNCharacterStream", "setNClob", "setRef", "setSQLXML", "setUnicodeStream");

    private final boolean ignorable;

    public PreparedStatementSetterPoint(boolean ignorable) {
        this.ignorable = ignorable;
    }

    @Override
    public ElementMatcher<MethodDescription> getMethodsMatcher() {
        ElementMatcher.Junction<MethodDescription> matcher = none();

        Set<String> setters = ignorable ? PS_IGNORABLE_SETTERS : PS_SETTERS;
        for (String setter : setters) {
            matcher = matcher.or(named(setter));
        }

        return matcher;
    }

    @Override
    public String getMethodsAround() {
        return ignorable ?
                "com.jimmy.friday.agent.plugin.action.method.jdbc.JdbcPreparedStatementIgnorableSetterAction"
                : "com.jimmy.friday.agent.plugin.action.method.jdbc.JdbcPreparedStatementSetterAction";
    }

    @Override
    public boolean isOverrideArgs() {
        return false;
    }
}
