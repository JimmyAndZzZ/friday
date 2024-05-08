package com.jimmy.friday.boot.enums.gateway;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrpcMethodEnum {

    CALL("call", true),
    ASYNC_CALL("asyncCall", false);

    private String methodName;

    private Boolean isSync;
}
