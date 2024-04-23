package com.jimmy.friday.demo.controller;

import com.jimmy.friday.boot.annotations.Trace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/ggg")
    @Trace
    public String ggg(@RequestParam("ii") String ii) {
        log.info("发送:{}", ii);

        rabbitTemplate.convertAndSend("simple-queue", ii);
        return "123";
    }

    @RabbitListener(queues = {"simple-queue"})
    public void getSimpleQueueMessage(@Payload String msg) {
        log.info("收到消息：{}", msg);
    }
}
