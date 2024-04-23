package com.jimmy.friday.agent.plugin.define.jdbc.mysql;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.plugin.define.jdbc.PreparedStatementNullSetterPoint;
import com.jimmy.friday.agent.plugin.define.jdbc.PreparedStatementSetterPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.jimmy.friday.agent.match.NameMatch.byName;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class MysqlPreparedStatementPluginDefine extends AbstractMysqlPluginDefine {

    private static final String MYSQL_PREPARED_STATEMENT_CLASS_NAME = "com.mysql.jdbc.PreparedStatement";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("execute").or(named("executeQuery"))
                                .or(named("executeLargeUpdate"));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.jdbc.mysql.MysqlPreparedStatementAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                },
                new PreparedStatementNullSetterPoint(),
                new PreparedStatementSetterPoint(false),
                new PreparedStatementSetterPoint(true)};
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName(MYSQL_PREPARED_STATEMENT_CLASS_NAME);
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[0];
    }
}
