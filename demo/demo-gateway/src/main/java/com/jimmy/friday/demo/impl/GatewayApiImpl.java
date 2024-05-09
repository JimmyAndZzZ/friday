package com.jimmy.friday.demo.impl;

import cn.hutool.core.date.DateUtil;
import com.jimmy.friday.demo.dto.ResultDTO;
import com.jimmy.friday.demo.dto.TestDTO;
import com.jimmy.friday.demo.service.GatewayApi;
import com.jimmy.friday.framework.Channel;
import com.jimmy.friday.framework.annotation.gateway.Api;
import com.jimmy.friday.framework.annotation.gateway.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@GatewayService
public class GatewayApiImpl implements GatewayApi {
    @Override
    @Api(id = "hello1")
    public String hello(Integer iii) {
        log.info("收到收到{}", iii);
        return "收到啦~~~~~";
    }

    @Api(retry = 3, timeout = 300, id = "hello2")
    @Override
    public ResultDTO hello(TestDTO testDTO, Date date) {
        log.info("收到testDTO:{}", testDTO);
        log.info("收到日期:{}", DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setId("qqqqq");
        return resultDTO;
    }

    @Override
    @Api(retry = 3, timeout = 300, id = "hello4")
    public String hello() {
        return "succ";
    }

    @Api(retry = 3, timeout = 300, id = "hello3")
    @Override
    public Map<String, Integer> hello(List<Date> dates, Set<String> list) {
        log.info("收到list:{}", list);

        for (Date date : dates) {
            log.info("收到日期:{}", DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
        }

        Map<String, Integer> resultDTO = new HashMap<>();
        resultDTO.put("qqqqq", 1111);

        Channel.push().setMessage("test message").finish();
        return resultDTO;
    }
}
