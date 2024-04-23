package com.jimmy.friday.agent.plugin.define.spring.rabbitmq;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.HierarchyMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.jimmy.friday.agent.match.ArgumentTypeNameMatch.takesArgumentWithType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class SpringRabbitMQConsumerPluginDefine extends BaseEnhancePluginDefine {

    private static final String ENHANCE_METHOD_DISPATCH = "executeListener";

    private static final String ENHANCE_CLASS_PRODUCER = "org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(ENHANCE_METHOD_DISPATCH).and(takesArgumentWithType(0, "com.rabbitmq.client.Channel"));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.spring.rabbitmq.SpringRabbitMQConsumerMethodAroundAction";
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
        return HierarchyMatch.byHierarchyMatch(new String[]{ENHANCE_CLASS_PRODUCER});
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[0];
    }
}
