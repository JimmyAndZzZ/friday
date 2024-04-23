package com.jimmy.friday.protocol.annotations;

import java.lang.annotation.*;

@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Listen {

    Protocol protocol();

    boolean isAck() default false;
}
