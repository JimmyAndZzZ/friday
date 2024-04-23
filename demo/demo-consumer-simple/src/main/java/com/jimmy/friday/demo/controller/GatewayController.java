package com.jimmy.friday.demo.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.jimmy.friday.boot.base.Callback;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.client.Channel;
import com.jimmy.friday.client.Gateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @GetMapping("/upload2")
    public String upload2() {
        GatewayResponse execute = Gateway.http().setServer("127.0.0.1:11211").setAppId("test").setServiceName("http").setFile(FileUtil.file("C:\\tmp\\hm.PDF")).setMethodId("uploadDemo").setClientName("client").execute();
        return execute.toString();
    }

    @GetMapping("/listen")
    public void listen() {
        Channel.sub("http").setAppId("test").setServer("127.0.0.1:11211").listen(message -> log.info("收到消息了:{}", message));
    }

    @GetMapping("/cancel")
    public void cancel() {
        Channel.sub("http").setAppId("test").setServer("127.0.0.1:11211").cancel();
    }

}
