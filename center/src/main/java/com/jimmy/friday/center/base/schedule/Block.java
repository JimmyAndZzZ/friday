package com.jimmy.friday.center.base.schedule;

import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.center.entity.ScheduleJob;

public interface Block {

    void block(ScheduleJob scheduleJob);

    BlockHandlerStrategyTypeEnum type();

    void release(Long id);
}
