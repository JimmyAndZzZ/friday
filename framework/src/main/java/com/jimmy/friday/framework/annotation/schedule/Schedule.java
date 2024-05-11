package com.jimmy.friday.framework.annotation.schedule;

import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Schedule {

    String id();

    String cron();

    int timeout() default 0;

    int retry() default 0;

    BlockHandlerStrategyTypeEnum BlockHandlerStrategyType() default BlockHandlerStrategyTypeEnum.SERIAL;
}
