package com.jimmy.friday.framework.annotation;

import cn.hutool.core.util.StrUtil;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Api {

    int retry() default 0;

    int timeout() default 0;

    String id();

    String desc() default StrUtil.EMPTY;

    String fallbackMethod() default StrUtil.EMPTY;

    Class<?> fallbackClass() default void.class;

    Class<? extends Throwable>[] ignoreExceptions() default {};
}
