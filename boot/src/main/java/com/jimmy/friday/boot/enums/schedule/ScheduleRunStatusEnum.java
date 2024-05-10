package com.jimmy.friday.boot.enums.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleRunStatusEnum {

    RUNNING("0"),
    SUCCESS("1"),
    ERROR("2"),
    TIMEOUT("3");

    private String code;

}
