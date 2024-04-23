package com.jimmy.friday.agent.plugin.define.jdbc.mysql;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.base.StaticMethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.jimmy.friday.agent.match.NameMatch.byName;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class MysqlConnectionImplCreatePluginDefine extends AbstractMysqlPluginDefine {

    private static final String JDBC_ENHANCE_CLASS = "com.mysql.jdbc.ConnectionImpl";

    private static final String CONNECT_METHOD = "getInstance";

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[]{
                new StaticMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(CONNECT_METHOD);
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.jdbc.mysql.MysqlConnectionCreate5xAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("prepareStatement");
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.jdbc.mysql.MysqlCreatePreparedStatementAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                },
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("createStatement");
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.jdbc.mysql.MysqlCreateStatementAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }};
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName(JDBC_ENHANCE_CLASS);
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return null;
    }
}
