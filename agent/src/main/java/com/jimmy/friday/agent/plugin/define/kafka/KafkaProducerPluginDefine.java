package com.jimmy.friday.agent.plugin.define.kafka;

import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.NameMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.jimmy.friday.agent.match.ArgumentTypeNameMatch.takesArgumentWithType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class KafkaProducerPluginDefine extends BaseEnhancePluginDefine {

    private static final String ENHANCE_METHOD = "doSend";

    private static final String ENHANCE_CLASS = "org.apache.kafka.clients.producer.KafkaProducer";

    private static final String CONSTRUCTOR_CLASS = "org.apache.kafka.clients.producer.ProducerConfig";

    @Override
    public MethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new MethodsInterceptPoint[]{
                new MethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(ENHANCE_METHOD);
                    }

                    @Override
                    public String getMethodsAround() {
                        return "com.jimmy.friday.agent.plugin.action.method.kafka.KafkaProducerMethodAroundAction";
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
        return NameMatch.byName(ENHANCE_CLASS);
    }

    @Override
    public ConstructsInterceptPoint[] getConstructsInterceptPoints() {
        return new ConstructsInterceptPoint[]{
                new ConstructsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return takesArgumentWithType(0, CONSTRUCTOR_CLASS);
                    }

                    @Override
                    public String getConstructorAround() {
                        return "com.jimmy.friday.agent.plugin.action.construct.kafka.ProducerConstructorInterceptorAction";
                    }
                }
        };
    }
}
