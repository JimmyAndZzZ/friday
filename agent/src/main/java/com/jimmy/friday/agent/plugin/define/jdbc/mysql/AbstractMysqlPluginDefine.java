package com.jimmy.friday.agent.plugin.define.jdbc.mysql;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.base.StaticMethodsInterceptPoint;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;

public abstract class AbstractMysqlPluginDefine extends BaseEnhancePluginDefine {

    private static final String WITNESS_MYSQL_5X_CLASS = "com.mysql.jdbc.ConnectionImpl";

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return null;
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return null;
    }

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return null;
    }

    @Override
    public String[] witnessClasses() {
        return new String[]{WITNESS_MYSQL_5X_CLASS};
    }
}
