package com.jimmy.friday.demo;

import com.jimmy.friday.framework.annotation.gateway.EnableGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableGateway
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DemoGatewayApplication {
    public static void main(String[] args) {
        System.getProperties().put("friday.config.path", "/tmp/gateway.properties");
        SpringApplication.run(DemoGatewayApplication.class, args);
    }
}
