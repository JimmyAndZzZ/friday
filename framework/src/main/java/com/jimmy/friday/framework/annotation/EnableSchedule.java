package com.jimmy.friday.framework.annotation;

import com.jimmy.friday.framework.config.BootstrapConfig;
import com.jimmy.friday.framework.other.GatewayImportSelector;
import com.jimmy.friday.framework.support.MethodScanSupport;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({BootstrapConfig.class})
@Documented
public @interface EnableSchedule {

    String[] basePackages() default {};
}
