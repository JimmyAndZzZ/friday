package com.jimmy.friday.protocol.core;

import lombok.Data;
import org.apache.kafka.common.header.Headers;

@Data
public class InputMessage {

    private long offset;

    private String message;

    private Headers headers;
}
