package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BlockHandlerStrategyTypeEnum {

    SERIAL("0", "单机串行"),
    DISCARD_SUBSEQUENT("1", "丢弃后续"),
    COVER_PREVIOUS("2", "覆盖之前");

    private String code;

    private String message;
}
