package com.jimmy.friday.agent.plugin.define.rabbitmq;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.HierarchyMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.jimmy.friday.agent.match.ArgumentTypeNameMatch.takesArgumentWithType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class RabbitMQConsumerPluginDefine extends BaseEnhancePluginDefine {

    private static final String ENHANCE_METHOD_DISPATCH = "handleDelivery";

    private static final String ENHANCE_CLASS_PRODUCER = "com.rabbitmq.client.Consumer";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(ENHANCE_METHOD_DISPATCH).and(takesArgumentWithType(2, "com.rabbitmq.client.AMQP$BasicProperties"));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.rabbitmq.RabbitMQConsumerMethodAroundAction";
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
