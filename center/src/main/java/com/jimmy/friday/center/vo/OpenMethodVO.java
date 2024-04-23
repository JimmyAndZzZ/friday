package com.jimmy.friday.center.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OpenMethodVO implements Serializable {

    private Long id;

    private String name;

    private String description;

    private String example;

    private String type;

    private String code;

    private String isFree;
}
