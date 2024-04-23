package com.jimmy.friday.demo.service;


import com.jimmy.friday.demo.dto.TestDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Dubbo2Api {

    List<String> ggg(List<String> list, TestDTO dto, String aaa);

    Date dddd(List<Date> list, Map<String, Date> d1, Map<String, Double> d2);

    void aaa();

    String bbb();

    String ccc(int i, long b);
}
