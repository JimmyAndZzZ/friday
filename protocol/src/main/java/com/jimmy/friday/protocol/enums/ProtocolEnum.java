package com.jimmy.friday.protocol.enums;

import cn.hutool.core.convert.Convert;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProtocolEnum {

    RABBITMQ(0, "rabbitMQ"),
    KAFKA(1, "kafka"),
    WEBSOCKET(2, "webSocket"),
    REDIS(3, "redis"),
    MEMORY(4, "memory");

    private Integer code;

    private String message;

    public static ProtocolEnum queryByCode(String code) {
        for (ProtocolEnum value : ProtocolEnum.values()) {
            if (value.code.equals(Convert.toInt(code))) {
                return value;
            }
        }

        return null;
    }

}
