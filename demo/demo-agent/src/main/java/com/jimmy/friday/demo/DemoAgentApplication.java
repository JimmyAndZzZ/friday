package com.jimmy.friday.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DemoAgentApplication {
    public static void main(String[] args) {
        System.getProperties().put("friday.config.path", "/tmp/agent.properties");
        SpringApplication.run(DemoAgentApplication.class, args);
    }
}
