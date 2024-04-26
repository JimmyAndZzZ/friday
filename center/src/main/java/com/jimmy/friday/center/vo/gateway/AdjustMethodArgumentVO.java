package com.jimmy.friday.center.vo.gateway;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdjustMethodArgumentVO implements Serializable {

    private String serviceId;

    private Integer retry;

    private Integer timeout;

    private String methodId;

    private String serviceType;

    private String desc;

    private String remark;

    private String isOpen;

    private String example;
}
