package com.jimmy.friday.framework.annotation.transaction;

import com.jimmy.friday.boot.enums.transaction.TransactionPropagationEnum;
import com.jimmy.friday.boot.enums.transaction.TransactionTypeEnum;

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
