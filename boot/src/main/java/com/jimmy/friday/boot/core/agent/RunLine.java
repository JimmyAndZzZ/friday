package com.jimmy.friday.boot.core.agent;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RunLine implements Serializable {

    private String method;

    private String clazz;

    private Long cost;

    private Boolean isException = false;

    private Integer exceptionLineCount;

    private String param;

    private Object returnValue;
}
