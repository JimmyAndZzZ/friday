package com.jimmy.friday.center.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MetricsVO implements Serializable {

    private String meterDate;

    private Integer invokeCount;

}
