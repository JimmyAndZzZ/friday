package com.jimmy.friday.center.utils;

import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.center.exception.OpenApiException;

public class Assert {

    private Assert() {

    }

    public static void state(boolean expression, ExceptionEnum exceptionEnum) {
        if (!expression) {
            throw new OpenApiException(exceptionEnum.getCode(), exceptionEnum.getMessage());
        }
    }

    public static void state(boolean expression, ExceptionEnum exceptionEnum, Object... append) {
        if (!expression) {
            throw new OpenApiException(exceptionEnum.getCode(), StrUtil.format(exceptionEnum.getMessage(), append));
        }
    }
}
