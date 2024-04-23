package com.jimmy.friday.center.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdjustServiceArgumentVO implements Serializable {

    private String serviceId;

    private Integer weight = 0;

    private String serviceType;
    
}
