package com.jimmy.friday.center.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {

    String topic();

    String groupId() default "";
}
