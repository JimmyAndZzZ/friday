package com.jimmy.friday.demo;

import com.google.common.collect.Lists;
import com.jimmy.friday.demo.dto.TestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DubboFallback {

    public List<String> gggFallback(List<String> list, TestDTO dto, String aaa) {
        log.info("收到dubbo请求：{},{},{}", list, dto, aaa);
        return Lists.newArrayList("fallback");
    }
}
