package com.jimmy.friday.demo.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.demo.DubboFallback;
import com.jimmy.friday.demo.dto.TestDTO;
import com.jimmy.friday.demo.service.Dubbo2Api;
import com.jimmy.friday.framework.annotation.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@DubboService(version = "1.0.0", retries = 0, timeout = 60 * 1000)
public class Dubbo2ApiImpl implements Dubbo2Api {

    @Override
    @Api(id = "ggg", fallbackMethod = "gggFallback", fallbackClass = DubboFallback.class)
    public List<String> ggg(List<String> list, TestDTO dto, String aaa) {
        if (StrUtil.isNotEmpty("!23")) {
            throw new RuntimeException("123");
        }

        return Lists.newArrayList("success");
    }

    @Override
    @Api(id = "dddd")
    public Date dddd(List<Date> list, Map<String, Date> d1, Map<String, Double> d2) {
        for (Date date : list) {
            log.info("收到日期:{}", DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
        }

        d1.forEach((k, v) -> log.info("收到map1:key:{},v:{}", k, DateUtil.format(v, "yyyy-MM-dd HH:mm:ss")));
        log.info("收到map2:{}", d2);
        return new Date();
    }

    @Override
    public void aaa() {
        log.info("收到啦111aaaa");
    }

    @Override
    @Api(id = "bbb")
    public String bbb() {
        log.info("收到了bbbbb");
        return "bbb";
    }

    @Override
    @Api(id = "ccc")
    public String ccc(int i, long b) {
        log.info("cccc:{},{}", i, b);
        return "succcccccc";
    }
}
