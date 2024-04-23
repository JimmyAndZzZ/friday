package com.jimmy.friday.agent.plugin.define.base;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.AllMethodMatch;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.IndirectMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;


public class CmdEnhancePluginDefine extends BaseEnhancePluginDefine {

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        MethodsInterceptPoint methodsInterceptPoint = new MethodsInterceptPoint() {

            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return AllMethodMatch.build();
            }

            @Override
            public String getMethodsAround() {
                return "com.jimmy.friday.agent.plugin.action.method.CmdMethodsAroundAction";
            }

            @Override
            public boolean isOverrideArgs() {
                return false;
            }
        };

        return new MethodsInterceptPoint[]{methodsInterceptPoint};
    }

    @Override
    public ClassMatch enhanceClass() {
        return new IndirectMatch() {

            @Override
            public ElementMatcher.Junction buildJunction() {
                return ElementMatchers.any();
            }

            @Override
            public boolean isMatch(TypeDescription typeDescription) {
                return true;
            }
        };
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[0];
    }
}
