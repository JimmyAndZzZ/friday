package com.jimmy.friday.center.config;

import com.jimmy.friday.center.utils.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(RabbitMQConstants.DELAYED_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLX_ROUTE_KEY)
                .withArgument("x-message-ttl", 30 * 60 * 1000) // 设置消息的 TTL，单位为毫秒
                .build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(RabbitMQConstants.DLX_EXCHANGE_NAME);
    }

    @Bean
    public Queue dlxQueue() {
        return new Queue(RabbitMQConstants.DLX_QUEUE_NAME);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(RabbitMQConstants.DLX_ROUTE_KEY);
    }
}
