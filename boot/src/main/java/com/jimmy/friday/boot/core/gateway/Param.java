package com.jimmy.friday.boot.core.gateway;

import lombok.Data;

import java.io.Serializable;

@Data
public class Param implements Serializable {

    private String name;

    private String type;

    private String display;

    private String desc;

    private String defaultValue;

    private Boolean isRequire;

}
