package com.jimmy.friday.boot.core.agent;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Command implements Serializable {

    private Long traceId;

    private String command;

    private List<String> param;
}
