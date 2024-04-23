package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoadTypeEnum {

    RANDOM,
    WEIGHT,
    SINGLE_THREAD,
    BALANCE
}
