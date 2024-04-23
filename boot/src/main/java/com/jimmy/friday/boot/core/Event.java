package com.jimmy.friday.boot.core;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class Event implements Serializable {

    private String type;

    private String message;

    public Event(EventTypeEnum type, String message) {
        this.message = message;
        this.type = type.getCode();
    }

    public Event() {

    }
}
