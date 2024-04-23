package com.jimmy.friday.demo.vo;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class NewsContentVO {

    private Long id;

    private String content;

    private String contentText;

    private Date annoDate;

    private Map<String, Object> param = new HashMap<>();

}
