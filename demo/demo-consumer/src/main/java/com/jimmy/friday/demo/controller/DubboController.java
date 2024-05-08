package com.jimmy.friday.demo.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.demo.dto.TestDTO;
import com.jimmy.friday.demo.service.Dubbo2Api;
import com.jimmy.friday.framework.Gateway;
import com.jimmy.friday.framework.annotation.GatewayReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dubbo")
public class DubboController {

    @GatewayReference(type = ServiceTypeEnum.DUBBO, serviceName = "dubbo-provider")
    private Dubbo2Api dubbo2Api;

    @GetMapping("/ggg")
    public Object ggg() {
        TestDTO dto = new TestDTO();
        dto.setContent("123");
        dto.setId(111);

        List<String> ggg = dubbo2Api.ggg(Lists.newArrayList("111"), dto, "222");
        return ggg;
    }

    @GetMapping("/dddd")
    public Object dddd() {
        Map<String, Date> dateMap = Maps.newHashMap();
        dateMap.put("11", new Date());

        Map<String, Double> doubleMap = Maps.newHashMap();
        doubleMap.put("11", 22.222);

        GatewayResponse execute = Gateway.dubbo()
                .setServiceName("dubbo-provider")
                .setMethodId("dddd")
                .addInvokeParam("list", Lists.newArrayList(new Date()))
                .addInvokeParam("d1", dateMap)
                .addInvokeParam("d2", doubleMap)
                .setAppId("test")
                .execute();

        return execute.toString();
    }

    @GetMapping("/aaa")
    public void aaa() {
        dubbo2Api.aaa();
    }

    @GetMapping("/ccc")
    public Object ccc() {
        GatewayResponse execute = Gateway.dubbo()
                .setServiceName("dubbo-provider")
                .setMethodId("ccc")
                .setAppId("test")
                .addInvokeParam("i", 3)
                .addInvokeParam("b", 4L)
                .execute();

        return execute.toString();
    }

    @GetMapping("/bbb")
    public String bbb() {
        return dubbo2Api.bbb();
    }
}
