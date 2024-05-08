package com.jimmy.friday.framework.annotation;

import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.boot.other.GlobalConstants;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface GatewayReference {

    String version() default GlobalConstants.DEFAULT_VERSION;

    int timeout() default 0;

    int retries() default 2;

    ServiceTypeEnum type();

    String serviceName();
}
