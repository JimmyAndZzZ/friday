package com.jimmy.friday.boot.core.gateway;

import lombok.Data;

import java.io.Serializable;

@Data
public class GatewayResponse implements Serializable {

    private Boolean isSuccess;

    private String jsonResult;

    private String error;

    private Integer code;

    private String exceptionClass;

    public static GatewayResponse ok(String jsonResult) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setJsonResult(jsonResult);
        gatewayResponse.setIsSuccess(true);
        return gatewayResponse;
    }

    public static GatewayResponse fail(String error) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setIsSuccess(false);
        gatewayResponse.setError(error);
        return gatewayResponse;
    }

    public static GatewayResponse fail(String error, Integer code) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setIsSuccess(false);
        gatewayResponse.setCode(code);
        gatewayResponse.setError(error);
        return gatewayResponse;
    }

    public static GatewayResponse fail(Exception e) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setIsSuccess(false);
        gatewayResponse.setError(e.getMessage());
        gatewayResponse.setExceptionClass(e.getClass().getName());
        return gatewayResponse;
    }
}
