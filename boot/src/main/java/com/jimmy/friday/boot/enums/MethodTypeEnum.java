package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MethodTypeEnum {

    QUERY("0"),
    POST("1"),
    FILE("2");

    private String code;

    public static MethodTypeEnum queryByType(String code) {
        for (MethodTypeEnum value : MethodTypeEnum.values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        return null;
    }
}
