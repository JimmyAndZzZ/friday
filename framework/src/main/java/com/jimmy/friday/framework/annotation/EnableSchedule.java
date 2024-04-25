package com.jimmy.friday.framework.annotation;

import com.jimmy.friday.framework.config.BootstrapConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({BootstrapConfig.class})
@Documented
public @interface EnableSchedule {

    String[] basePackages() default {};
}
