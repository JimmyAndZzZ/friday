package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YesOrNoEnum {

    YES("1"), NO("0");

    private String code;
}
