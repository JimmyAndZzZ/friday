package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobRunStatusEnum {

    RUNNING("0"),
    SUCCESS("1"),
    ERROR("2"),
    TIMEOUT("3");

    private String code;

}
