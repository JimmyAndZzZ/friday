package com.jimmy.friday.demo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestDTO implements Serializable {

    private String content;

    private Integer id;
}
