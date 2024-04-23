package com.jimmy.friday.agent.plugin.define.rabbitmq;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ArgumentTypeNameMatch;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.MultiClassNameMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.jimmy.friday.agent.match.ArgumentTypeNameMatch.takesArgumentWithType;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class ChannelNPluginDefine extends BaseEnhancePluginDefine {

    private static final String CONSUME_ENHANCE_METHOD = "basicConsume";

    private static final String ENHANCE_METHOD_DISPATCH = "basicPublish";

    private static final String ENHANCE_CLASS_PRODUCER = "com.rabbitmq.client.impl.ChannelN";

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[]{
                new ConstructsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return takesArgumentWithType(0, "com.rabbitmq.client.impl.AMQConnection");
                    }

                    @Override
                    public String getConstructorAround() {
                        return "com.jimmy.friday.agent.plugin.action.construct.rabbitmq.ChannelNConstructInterceptorAction";
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
                        return named(ENHANCE_METHOD_DISPATCH).and(ArgumentTypeNameMatch.takesArgumentWithType(4, "com.rabbitmq.client.AMQP$BasicProperties"));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.rabbitmq.ChannelNPublishMethodAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return true;
                    }
                },
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(CONSUME_ENHANCE_METHOD).and(takesArguments(7));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.rabbitmq.ChannelNConsumerMethodAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return true;
                    }
                }
        };
    }

    @Override
    public ClassMatch enhanceClass() {
        return MultiClassNameMatch.byMultiClassMatch(ENHANCE_CLASS_PRODUCER);
    }

}
