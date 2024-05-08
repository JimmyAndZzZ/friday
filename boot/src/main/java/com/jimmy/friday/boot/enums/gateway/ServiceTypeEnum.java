package com.jimmy.friday.boot.enums.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceTypeEnum {

    DUBBO(LoadTypeEnum.RANDOM),
    SPRING_CLOUD(LoadTypeEnum.RANDOM),
    HTTP(LoadTypeEnum.BALANCE),
    GATEWAY(LoadTypeEnum.WEIGHT),
    GRPC(LoadTypeEnum.SINGLE_THREAD);

    private LoadTypeEnum loadTypeEnum;

    public static ServiceTypeEnum queryByType(String type) {
        for (ServiceTypeEnum value : ServiceTypeEnum.values()) {
            if (value.toString().equalsIgnoreCase(type)) {
                return value;
            }
        }

        return null;
    }
}
