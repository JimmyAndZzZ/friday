package com.jimmy.friday.agent.plugin.define.http;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.jimmy.friday.agent.match.NameMatch.byName;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class HutoolHttpPluginDefine extends BaseEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "cn.hutool.http.HttpRequest";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("execute").and(takesArguments(1));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.http.HutoolHttpExecuteAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return null;
    }
}
