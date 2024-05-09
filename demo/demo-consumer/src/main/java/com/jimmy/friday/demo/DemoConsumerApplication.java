package com.jimmy.friday.demo;

import com.jimmy.friday.framework.annotation.gateway.EnableGateway;
import com.jimmy.friday.framework.annotation.transaction.EnableTransactional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableGateway
@EnableTransactional
@SpringBootApplication
public class DemoConsumerApplication {
    public static void main(String[] args) {
        System.getProperties().put("friday.config.path", "/tmp/consumer.properties");
        SpringApplication.run(DemoConsumerApplication.class, args);
    }
}
