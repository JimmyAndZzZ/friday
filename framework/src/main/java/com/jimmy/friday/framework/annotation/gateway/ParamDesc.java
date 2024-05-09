package com.jimmy.friday.framework.annotation.gateway;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamDesc {

    String desc();

    String value() default "";

    boolean isRequire() default true;
}
