package com.jimmy.friday.agent.plugin.define.dubbo;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.bytebuddy.support.WitnessMethod;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.NameMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;

public class Dubbo2EnhancePluginDefine extends BaseEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "org.apache.dubbo.monitor.support.MonitorFilter";

    private static final String CONTEXT_TYPE_NAME = "org.apache.dubbo.rpc.RpcContext";

    private static final String GET_SERVER_CONTEXT_METHOD_NAME = "getServerContext";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("invoke");
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.dubbo.Dubbo2MethodsAroundAction";
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
        return NameMatch.byName(ENHANCE_CLASS);
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[0];
    }

    @Override
    public List<WitnessMethod> witnessMethods() {
        return Collections.singletonList(new WitnessMethod(
                CONTEXT_TYPE_NAME,
                named(GET_SERVER_CONTEXT_METHOD_NAME).and(
                        returns(named(CONTEXT_TYPE_NAME)))
        ));
    }
}
