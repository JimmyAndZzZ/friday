package com.jimmy.friday.framework.config;

import com.jimmy.friday.framework.process.schedule.ScheduleInterruptProcess;
import com.jimmy.friday.framework.process.schedule.ScheduleInvokeProcess;
import com.jimmy.friday.framework.schedule.ScheduleCenter;
import com.jimmy.friday.framework.schedule.ScheduleExecutor;
import com.jimmy.friday.framework.support.TransmitSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleConfig {

    @Bean
    public ScheduleCenter scheduleCenter() {
        return new ScheduleCenter();
    }

    @Bean
    public ScheduleExecutor scheduleExecutor(TransmitSupport transmitSupport, ApplicationContext applicationContext) {
        return new ScheduleExecutor(this.scheduleCenter(), transmitSupport, applicationContext);
    }

    @Configuration
    protected static class ProcessConfig {

        @Bean
        public ScheduleInvokeProcess scheduleInvokeProcess(ScheduleExecutor scheduleExecutor) {
            return new ScheduleInvokeProcess(scheduleExecutor);
        }

        @Bean
        public ScheduleInterruptProcess scheduleInterruptProcess(ScheduleExecutor scheduleExecutor) {
            return new ScheduleInterruptProcess(scheduleExecutor);
        }
    }
}
