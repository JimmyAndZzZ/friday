package com.jimmy.friday.boot.core.schedule;

import lombok.Data;

import java.io.Serializable;


@Data
public class ScheduleExecutor implements Serializable {

    private String applicationName;

    private String ip;

    private Long lastInvokeTime;

    private Integer weight;

    private String applicationId;

    private Long id;
}
