package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChargeTypeEnum {

    TIME("0"), PAGE("1");

    private String code;

    public static ChargeTypeEnum queryByType(String code) {
        for (ChargeTypeEnum value : ChargeTypeEnum.values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        return null;
    }
}
