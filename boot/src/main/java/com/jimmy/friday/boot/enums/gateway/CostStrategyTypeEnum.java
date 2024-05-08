package com.jimmy.friday.boot.enums.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CostStrategyTypeEnum {

    COMMON("0", "普通"), FILE("1", "文件");

    private String code;

    private String message;
}
