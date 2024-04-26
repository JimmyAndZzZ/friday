package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleStatusEnum {

    CLOSE("0"),
    OPEN("1");

    private String code;
}
