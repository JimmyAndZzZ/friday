package com.jimmy.friday.boot.core;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.other.ShortUUID;
import lombok.Data;

import java.io.Serializable;

@Data
public class Event implements Serializable {

    private String id;

    private String type;

    private String message;

    public Event(EventTypeEnum type, String message) {
        super();
        this.message = message;
        this.type = type.getCode();
    }

    public Event() {
        this.id = ShortUUID.uuid();
    }
}
