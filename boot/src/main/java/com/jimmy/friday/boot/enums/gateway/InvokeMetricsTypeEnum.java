package com.jimmy.friday.boot.enums.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvokeMetricsTypeEnum {

    EVERYDAY("0"),
    HISTORY("1");

    private String code;
}
