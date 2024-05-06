package com.jimmy.friday.center.core.schedule.block;

import com.jimmy.friday.boot.enums.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.center.base.schedule.Block;
import com.jimmy.friday.center.entity.ScheduleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DiscardSubsequent implements Block {

    @Override
    public void block(ScheduleJob scheduleJob) {

    }

    @Override
    public BlockHandlerStrategyTypeEnum type() {
        return BlockHandlerStrategyTypeEnum.DISCARD_SUBSEQUENT;
    }


}
