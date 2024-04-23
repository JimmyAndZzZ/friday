package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceStatusEnum {
    ALIVE("0"),
    ABNORMAL("1"),
    DISCONNECT("2");

    private String code;
}
