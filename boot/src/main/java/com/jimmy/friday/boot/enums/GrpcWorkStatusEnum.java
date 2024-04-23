package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrpcWorkStatusEnum {
    ALIVE,
    BUSY,
    DEAD
}
