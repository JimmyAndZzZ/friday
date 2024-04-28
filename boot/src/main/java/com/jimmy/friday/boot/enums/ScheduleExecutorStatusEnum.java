package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleExecutorStatusEnum {
    ALIVE("0"),
    DISCONNECT("1");

    private String code;
}
