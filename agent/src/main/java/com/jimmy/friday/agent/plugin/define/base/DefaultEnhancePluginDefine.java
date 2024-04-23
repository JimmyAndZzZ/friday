package com.jimmy.friday.agent.plugin.define.base;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.MethodNameMatch;
import com.jimmy.friday.agent.match.NameMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class DefaultEnhancePluginDefine extends BaseEnhancePluginDefine {

    private String classPath;

    private Set<String> methods;

    public DefaultEnhancePluginDefine(String classPath, Set<String> methods) {
        this.classPath = classPath;
        this.methods = methods;
    }

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        List<MethodsInterceptPoint> collect = methods.stream().map(method -> {
            MethodsInterceptPoint methodsInterceptPoint = new MethodsInterceptPoint() {

                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return MethodNameMatch.expressionBuild(method);
                }

                @Override
                public String getMethodsAround() {
                    return "com.jimmy.friday.agent.plugin.action.method.EnhanceMethodsAroundAction";
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            };

            return methodsInterceptPoint;
        }).collect(Collectors.toList());

        return collect.toArray(new MethodsInterceptPoint[collect.size()]);
    }

    @Override
    public ClassMatch enhanceClass() {
        return NameMatch.byName(classPath);
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[0];
    }
}
