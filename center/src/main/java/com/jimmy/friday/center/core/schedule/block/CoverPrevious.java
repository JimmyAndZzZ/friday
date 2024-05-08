package com.jimmy.friday.center.core.schedule.block;

import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.center.Schedule;
import com.jimmy.friday.center.base.schedule.Block;
import com.jimmy.friday.center.entity.ScheduleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoverPrevious implements Block {

    @Autowired
    private Schedule schedule;

    @Override
    public void block(ScheduleJob scheduleJob) {
        schedule.submit(scheduleJob);
    }

    @Override
    public BlockHandlerStrategyTypeEnum type() {
        return BlockHandlerStrategyTypeEnum.COVER_PREVIOUS;
    }
}
