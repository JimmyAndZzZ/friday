package com.jimmy.friday.demo;

import com.jimmy.friday.framework.annotation.schedule.EnableSchedule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableSchedule
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DemoScheduleApplication {
    public static void main(String[] args) {
        System.getProperties().put("friday.config.path", "/tmp/schedule.properties");
        SpringApplication.run(DemoScheduleApplication.class, args);
    }
}
