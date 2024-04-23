package com.jimmy.friday.demo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue getSimpleQueue() {
        return new Queue("simple-queue");
    }
}
