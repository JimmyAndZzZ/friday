package com.jimmy.friday.framework.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DubboCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            return null != Class.forName("org.apache.dubbo.config.ServiceConfig");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
