package com.jimmy.friday.framework.annotation;

import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Schedule {

    String id();

    String cron();

    BlockHandlerStrategyTypeEnum BlockHandlerStrategyType() default BlockHandlerStrategyTypeEnum.SERIAL;
}
