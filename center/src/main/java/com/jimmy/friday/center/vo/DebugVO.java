package com.jimmy.friday.center.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DebugVO implements Serializable {

    private String appId;

    private Long methodId;

    private String json;

}
