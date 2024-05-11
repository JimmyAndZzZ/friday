package com.jimmy.friday.boot.core.schedule;

import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.boot.enums.schedule.ScheduleSourceEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ScheduleInfo implements Serializable {

    private String springBeanId;

    private String className;

    private String methodName;

    private String scheduleId;

    private String cron;

    private Long timeout;

    private Integer retry;

    private Integer shardingNum;

    private BlockHandlerStrategyTypeEnum blockHandlerStrategyType = BlockHandlerStrategyTypeEnum.SERIAL;

    private ScheduleSourceEnum scheduleSource;

}
