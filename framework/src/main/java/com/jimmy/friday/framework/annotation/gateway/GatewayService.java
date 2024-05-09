package com.jimmy.friday.framework.annotation.gateway;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface GatewayService {

}
