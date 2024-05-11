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
public class DiscardSubsequent implements Block {

    @Autowired
    private Schedule schedule;

    @Override
    public void block(ScheduleJob scheduleJob, Integer currentShardingNum) {
        if (schedule.isRunning(scheduleJob.getId(), currentShardingNum)) {
            log.error("调度未结束，此次调度丢弃,{}", scheduleJob.getId());
            return;
        }

        schedule.submit(scheduleJob, currentShardingNum);
    }

    @Override
    public BlockHandlerStrategyTypeEnum type() {
        return BlockHandlerStrategyTypeEnum.DISCARD_SUBSEQUENT;
    }

    @Override
    public void release(Long id) {

    }

}
