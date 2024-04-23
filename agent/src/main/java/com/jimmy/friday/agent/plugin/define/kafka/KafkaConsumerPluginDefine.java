package com.jimmy.friday.agent.plugin.define.kafka;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static com.jimmy.friday.agent.match.ArgumentTypeNameMatch.takesArgumentWithType;
import static com.jimmy.friday.agent.match.NameMatch.byName;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;

public class KafkaConsumerPluginDefine extends BaseEnhancePluginDefine {

    private static final String ENHANCE_METHOD = "pollOnce";

    private static final String SUBSCRIBE_METHOD = "subscribe";

    private static final String ENHANCE_COMPATIBLE_METHOD = "pollForFetches";

    private static final String ENHANCE_CLASS = "org.apache.kafka.clients.consumer.KafkaConsumer";

    private static final String CONSTRUCTOR_INTERCEPT_TYPE = "org.apache.kafka.clients.consumer.ConsumerConfig";

    private static final String CONSTRUCTOR_INTERCEPT_MAP_TYPE = "java.util.Map";

    private static final String SUBSCRIBE_INTERCEPT_TYPE_PATTERN = "java.util.regex.Pattern";

    private static final String SUBSCRIBE_INTERCEPT_TYPE_NAME = "java.util.Collection";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(ENHANCE_METHOD).or(named(ENHANCE_COMPATIBLE_METHOD).and(returns(Map.class)));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.kafka.KafkaConsumerMethodAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                },
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(SUBSCRIBE_METHOD)
                                .and(takesArgumentWithType(0, SUBSCRIBE_INTERCEPT_TYPE_NAME));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.kafka.KafkaSubscribeMethodAroundAction";
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                },
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(SUBSCRIBE_METHOD)
                                .and(takesArgumentWithType(0, SUBSCRIBE_INTERCEPT_TYPE_PATTERN));
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.kafka.KafkaSubscribeMethodAroundAction";
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
        return new ConstructsInterceptPoint[]{
                new ConstructsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return takesArgumentWithType(0, CONSTRUCTOR_INTERCEPT_TYPE);
                    }

                    @Override
                    public String getConstructorAround() {
                        return "com.jimmy.friday.agent.plugin.action.construct.kafka.ConstructorWithConsumerConfigInterceptAction";
                    }
                },
                new ConstructsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return takesArgumentWithType(0, CONSTRUCTOR_INTERCEPT_MAP_TYPE);
                    }

                    @Override
                    public String getConstructorAround() {
                        return "com.jimmy.friday.agent.plugin.action.construct.kafka.ConstructorWithMapInterceptAction";
                    }
                },
        };
    }
}
