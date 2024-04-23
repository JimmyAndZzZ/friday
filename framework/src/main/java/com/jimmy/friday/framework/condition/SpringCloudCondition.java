package com.jimmy.friday.framework.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SpringCloudCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            return null != Class.forName("org.springframework.cloud.openfeign.FeignClientBuilder");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
