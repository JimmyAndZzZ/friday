package com.jimmy.friday.framework.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class HttpCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            return null != Class.forName("org.springframework.web.servlet.DispatcherServlet");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
