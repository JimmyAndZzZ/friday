package com.jimmy.friday.demo.controller;

import com.jimmy.friday.framework.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
@RequestMapping("/push")
public class PushController {

    @GetMapping("/listen")
    public void listen() {
        Channel.sub("demo").setAppId("test1").listen(message -> log.info("test1收到消息拉:{}", message));
        Channel.sub("demo").setAppId("test2").listen(message -> log.info("test2收到消息拉:{}", message));
    }

    @GetMapping("/cancel")
    public void cancel() {
        Channel.sub("demo").setAppId("test1").cancel();
        Channel.sub("demo").setAppId("test2").cancel();
    }
}
