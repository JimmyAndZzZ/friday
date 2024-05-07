package com.jimmy.friday.framework.config;

import com.jimmy.friday.framework.bootstrap.ScheduleBootstrap;
import com.jimmy.friday.framework.callback.ScheduleCallback;
import com.jimmy.friday.framework.core.ConfigLoad;
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

    @Bean
    public ScheduleBootstrap scheduleBootstrap(TransmitSupport transmitSupport, ConfigLoad configLoad, ScheduleCenter scheduleCenter, ScheduleExecutor scheduleExecutor) {
        return new ScheduleBootstrap(transmitSupport, configLoad, scheduleCenter, scheduleExecutor);
    }

    @Bean
    public ScheduleCallback scheduleCallback(ConfigLoad configLoad, ScheduleCenter scheduleCenter) {
        return new ScheduleCallback(configLoad, scheduleCenter);
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
