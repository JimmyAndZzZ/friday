package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderSourceTypeEnum {

    INSIDE("0", "内部");

    private String code;

    private String message;
}
