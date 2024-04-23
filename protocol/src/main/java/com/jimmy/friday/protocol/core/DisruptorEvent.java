package com.jimmy.friday.protocol.core;

import lombok.Data;

@Data
public class DisruptorEvent {

    private String data;

    private String topic;
}
