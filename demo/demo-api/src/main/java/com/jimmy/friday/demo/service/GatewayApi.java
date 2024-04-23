package com.jimmy.friday.demo.service;

import com.jimmy.friday.demo.dto.ResultDTO;
import com.jimmy.friday.demo.dto.TestDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GatewayApi {

    String hello(Integer iii);

    Map<String, Integer> hello(List<Date> dates, Set<String> list);

    ResultDTO hello(TestDTO testDTO, Date date);

    String hello();
}
