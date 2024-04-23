package com.jimmy.friday.framework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Condition {

    Class<? extends org.springframework.context.annotation.Condition> condition();
}
