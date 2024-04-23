package com.jimmy.friday.demo.controller;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.framework.Gateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/springCloud")
public class SpringCloudController {

    @GetMapping("/demo")
    public Object demo() {
        GatewayResponse execute = Gateway.springCloud()
                .setServiceName("spring_cloud")
                .setAppId("test")
                .setMethodId("get")
                .addInvokeParam("id", 1L).execute();
        return execute.toString();
    }

    @GetMapping("/demo1")
    public Object demo1() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("1", "ffff");

        GatewayResponse execute = Gateway.springCloud()
                .setServiceName("spring_cloud")
                .setAppId("test")
                .setMethodId("post")
                .addInvokeParam("id", 1L)
                .setBody(map).execute();
        return execute.toString();
    }
}
