package com.jimmy.friday.agent.plugin.define.base;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ArgumentTypeNameMatch;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.NameMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;


public class LoggerEnhancePluginDefine extends BaseEnhancePluginDefine {

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[0];
    }

    @Override
    public ClassMatch enhanceClass() {
        return NameMatch.byName("ch.qos.logback.classic.spi.LoggingEvent");
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[]{
                new ConstructsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return ArgumentTypeNameMatch.takesArgumentWithType(1, "ch.qos.logback.classic.Logger");
                    }

                    @Override
                    public String getConstructorAround() {
                        return "com.jimmy.friday.agent.plugin.action.construct.LoggerConstructInterceptorAction";
                    }
                }};
    }
}
