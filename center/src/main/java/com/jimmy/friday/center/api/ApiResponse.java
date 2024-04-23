package com.jimmy.friday.center.api;

import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.exception.OpenApiException;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiResponse implements Serializable {

    private Integer errorCode;

    private Object result;

    private String errorMsg;

    public static ApiResponse ok(Object result) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(result);
        apiResponse.setErrorCode(0);
        return apiResponse;
    }

    public static ApiResponse fail(ExceptionEnum exceptionEnum) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setErrorCode(exceptionEnum.getCode());
        apiResponse.setErrorMsg(exceptionEnum.getMessage());
        return apiResponse;
    }

    public static ApiResponse fail(OpenApiException openApiException) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setErrorCode(openApiException.getCode());
        apiResponse.setErrorMsg(openApiException.getMessage());
        return apiResponse;
    }

    public static ApiResponse fail(GatewayException gatewayException) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setErrorCode(ExceptionEnum.SYSTEM_ERROR.getCode());
        apiResponse.setErrorMsg(gatewayException.getMessage());
        return apiResponse;
    }

    public static ApiResponse fail(ExceptionEnum exceptionEnum, Object... format) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setErrorCode(exceptionEnum.getCode());
        apiResponse.setErrorMsg(StrUtil.format(exceptionEnum.getMessage(), format));
        return apiResponse;
    }
}
