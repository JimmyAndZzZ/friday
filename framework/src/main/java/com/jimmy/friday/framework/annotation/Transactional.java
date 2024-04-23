package com.jimmy.friday.framework.annotation;

import com.jimmy.friday.boot.enums.TransactionPropagationEnum;
import com.jimmy.friday.boot.enums.TransactionTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {

    int timeout() default 60;

    TransactionTypeEnum type();

    Class<?> executeClass() default Void.class;

    String confirmMethod() default "";

    String cancelMethod() default "";

    TransactionPropagationEnum propagation() default TransactionPropagationEnum.REQUIRED;
}