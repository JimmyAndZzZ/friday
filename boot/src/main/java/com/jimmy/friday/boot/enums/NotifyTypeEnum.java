package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotifyTypeEnum {

    PROGRESS, ERROR, COMPLETED, PENDING, TIME_OUT, CANCEL
}
