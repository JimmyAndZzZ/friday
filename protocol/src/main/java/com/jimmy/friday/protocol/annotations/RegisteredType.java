package com.jimmy.friday.protocol.annotations;

import com.jimmy.friday.protocol.enums.ProtocolEnum;
import org.springframework.context.annotation.Condition;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisteredType {

    ProtocolEnum type();

    Class<? extends Condition>[] condition() default {};
}
