package com.jimmy.friday.boot.enums.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderPurposeTypeEnum {

    BALANCE_RECHARGE("0", "余额充值");

    private String code;

    private String message;
}
