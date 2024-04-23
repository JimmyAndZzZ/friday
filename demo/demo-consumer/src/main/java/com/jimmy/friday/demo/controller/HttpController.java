package com.jimmy.friday.demo.controller;

import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.framework.Gateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/http")
public class HttpController {

    @GetMapping("/abc")
    public Object abc() {
        GatewayResponse execute = Gateway.http()
                .setServiceName("http")
                .setMethodId("abc")
                .setAppId("test")
                .setTimeout(3)
                .execute();
        return execute.toString();
    }

    @GetMapping("/bc")
    public Object bc() {
        GatewayResponse execute = Gateway.http()
                .setServiceName("http")
                .setMethodId("bc")
                .setAppId("test")
                .setTimeout(3)
                .addInvokeParam("id", 1L)
                .addInvokeParam("str", "!23")
                .addInvokeParam("ss", "456")
                .execute();
        return execute.toString();
    }
}
