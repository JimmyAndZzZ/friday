package com.jimmy.friday.framework.annotation.schedule;

import com.jimmy.friday.framework.config.BootstrapConfig;
import com.jimmy.friday.framework.other.schedule.ScheduleImportSelector;
import com.jimmy.friday.framework.other.schedule.ScheduleScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({BootstrapConfig.class, ScheduleImportSelector.class, ScheduleScanRegistrar.class})
@Documented
public @interface EnableSchedule {

    String[] basePackages() default {};
}
