package com.jimmy.friday.protocol.annotations;

import com.jimmy.friday.protocol.enums.ProtocolEnum;
import com.jimmy.friday.protocol.enums.RequestMethod;
import com.jimmy.friday.protocol.enums.SerializerTypeEnum;

import java.lang.annotation.*;


@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Protocol {

    String topic();

    String groupId() default "";

    ProtocolEnum protocol();

    RequestMethod requestMethod() default RequestMethod.GET;

    int port() default 0;

    int concurrentConsumers() default 10;

    int maxConcurrentConsumers() default 20;

    int batchSize() default 1000;

    SerializerTypeEnum serializerType() default SerializerTypeEnum.DEFAULT;
}
