package com.jimmy.friday.framework;

import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.framework.base.Job;
import com.jimmy.friday.framework.schedule.ScheduleCenter;

public class Schedule {

    private Schedule() {

    }

    public static AppendBuild append() {
        return new AppendBuild();
    }

    public static RemoveBuild remove() {
        return new RemoveBuild();
    }

    public static class AppendBuild {

        private Job job;

        private String cron;

        private Integer retry;

        private Long timeout;

        private String scheduleId;

        private BlockHandlerStrategyTypeEnum blockHandlerStrategyType = BlockHandlerStrategyTypeEnum.SERIAL;

        public AppendBuild setScheduleId(String scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        public AppendBuild setRetry(Integer retry) {
            this.retry = retry;
            return this;
        }

        public AppendBuild setTimeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public AppendBuild setCron(String cron) {
            this.cron = cron;
            return this;
        }

        public AppendBuild submit(Job job) {
            this.job = job;
            return this;
        }

        public AppendBuild setBlockHandlerStrategyType(BlockHandlerStrategyTypeEnum blockHandlerStrategyType) {
            this.blockHandlerStrategyType = blockHandlerStrategyType;
            return this;
        }

        public void append() {
            Boot.getApplicationContext().getBean(ScheduleCenter.class).register(
                    scheduleId,
                    cron,
                    job,
                    blockHandlerStrategyType,
                    timeout,
                    retry);
        }
    }

    public static class RemoveBuild {

        private String scheduleId;

        public RemoveBuild setScheduleId(String scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        public void remove() {
            Boot.getApplicationContext().getBean(ScheduleCenter.class).remove(scheduleId);
        }
    }

}
