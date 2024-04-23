package com.jimmy.friday.protocol.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            boolean b = null != Class.forName("org.redisson.config.Config");
            return b;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
