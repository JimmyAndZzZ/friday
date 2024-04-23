package com.jimmy.friday.agent.plugin.define.base;

import com.google.common.collect.Lists;
import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.HierarchyMatch;
import com.jimmy.friday.agent.match.IndirectMatch;
import com.jimmy.friday.agent.match.PrefixMatch;
import com.jimmy.friday.agent.match.logical.LogicalMatchOperation;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import com.jimmy.friday.boot.other.ShortUUID;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;
import java.util.Set;

import static com.jimmy.friday.agent.match.PrefixMatch.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class RunnablePluginDefine extends BaseEnhancePluginDefine {

    private static final String RUNNABLE_RUN_METHOD = "run";

    private static final String RUNNABLE_CLASS = "java.lang.Runnable";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(RUNNABLE_RUN_METHOD);
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.RunnableMethodsAroundAction";
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
        return LogicalMatchOperation.and(prefixesMatchesForJdkThreading(), HierarchyMatch.byHierarchyMatch(RUNNABLE_CLASS));
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[]{
                new ConstructsInterceptPoint() {

                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return ElementMatchers.any();
                    }

                    @Override
                    public String getConstructorAround() {
                        return "com.jimmy.friday.agent.plugin.action.construct.RunnableConstructInterceptorAction";
                    }
                }
        };
    }

    /**
     * 匹配条件构建
     *
     * @return
     */
    private IndirectMatch prefixesMatchesForJdkThreading() {
        Set<String> point = ConfigLoad.getDefault().getCollectorPath();
        List<PrefixMatch> prefixMatches = Lists.newArrayList();

        if (point != null && !point.isEmpty()) {
            for (String prefix : point) {
                if (prefix.startsWith("java.") || prefix.startsWith("javax.")) {
                    continue;
                }
                prefixMatches.add(nameStartsWith(prefix));
            }
        }

        return prefixMatches.size() == 0 ? LogicalMatchOperation.or(new PrefixMatch[]{nameStartsWith(ShortUUID.uuid())}) : LogicalMatchOperation.or(prefixMatches.toArray(new PrefixMatch[0]));
    }
}
