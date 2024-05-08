
package com.jimmy.friday.boot.enums.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleSourceEnum {

    ANNOTATION("0"),
    MANUAL("1");

    private String code;
}
