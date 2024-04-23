package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionEnum {
    MISS_PARAMETER(1001, "参数缺失:{}"),
    ERROR_PARAMETER(1002, "参数错误:{}"),
    ERROR_FILE(1003, "文件异常:{}"),
    ACCOUNT_ERROR(2001, "账号异常:{}"),
    ACCOUNT_BALANCE_INSUFFICIENT(2002, "余额不足"),
    METHOD_ERROR(3001, "方法异常:{}"),
    SERVICE_ERROR(4001, "服务异常:{}"),
    SYSTEM_ERROR(9999, "系统异常,{}");
    private int code;

    private String message;
}
