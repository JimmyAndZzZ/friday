package com.jimmy.friday.framework.annotation.gateway;

import com.jimmy.friday.framework.config.BootstrapConfig;
import com.jimmy.friday.framework.other.gateway.GatewayImportSelector;
import com.jimmy.friday.framework.other.gateway.GatewayScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({BootstrapConfig.class, GatewayScanRegistrar.class, GatewayImportSelector.class})
@Documented
public @interface EnableGateway {

    String[] basePackages() default {};
}
