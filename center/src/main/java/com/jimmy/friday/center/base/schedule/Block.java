package com.jimmy.friday.center.base.schedule;

import com.jimmy.friday.boot.enums.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.center.entity.ScheduleJob;

public interface Block {

    void block(ScheduleJob scheduleJob);

    BlockHandlerStrategyTypeEnum type();
}
