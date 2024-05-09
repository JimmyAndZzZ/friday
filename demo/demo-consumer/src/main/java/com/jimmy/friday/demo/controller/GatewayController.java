package com.jimmy.friday.demo.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.base.Callback;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.demo.service.GatewayApi;
import com.jimmy.friday.framework.Gateway;
import com.jimmy.friday.framework.annotation.gateway.GatewayReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @GatewayReference(type = ServiceTypeEnum.GATEWAY, serviceName = "demo")
    private GatewayApi gatewayApi;

    @GetMapping("/push")
    public Object push() {
        Map<String, Integer> hello1 = gatewayApi.hello(Lists.newArrayList(new Date()), Sets.newHashSet("123"));
        System.out.println("hello1" + hello1);
        return hello1.toString();
    }

    @GetMapping("/demo2")
    public Object demo2() {
        GatewayResponse execute = Gateway.gateway()
                .setServiceName("demo")
                .setMethodId("hello3")
                .setAppId("test")
                .addInvokeParam("list", Sets.newHashSet("123"))
                .addInvokeParam("dates", Lists.newArrayList(new Date()))
                .execute();

        return execute.toString();
    }

    @GetMapping("/demo3")
    public Object demo3() {
        String hello = gatewayApi.hello();
        return hello;
    }

    @GetMapping("/grpc")
    public Object grpc() {
        GatewayResponse execute = Gateway.grpc()
                .setServiceName("pyGwDemoService")
                .addInvokeParam("iii", 111)
                .setAppId("test")
                .execute();
        return execute.toString();
    }

    @GetMapping("/grpcAsync")
    public String grpcAsync() {
        GatewayResponse gatewayResponse = Gateway.grpc()
                .setServiceName("pyGwDemoService")
                .addInvokeParam("iii", 111)
                .setAppId("test")
                .asyncExecute(new Callback() {
                    @Override
                    public void progress(Integer progressRate) {
                        log.info("收到进度：{}", progressRate);
                    }

                    @Override
                    public void finish(Object result) {
                        log.info("收到结果：{}", result);
                    }

                    @Override
                    public void error(String errorMessage) {
                        log.info("调用失败：{}", errorMessage);
                    }
                });
        return gatewayResponse.toString();
    }
}
