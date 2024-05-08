package com.jimmy.friday.boot.enums.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceWarnTypeEnum {

    HEARTBEAT_ERROR("0", "服务心跳异常"),
    PROVIDER_OFFLINE("1", "服务下线");

    private String code;

    private String message;

    public static ServiceWarnTypeEnum queryByCode(String code) {
        for (ServiceWarnTypeEnum value : ServiceWarnTypeEnum.values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        return null;
    }

}
