package com.jimmy.friday.boot.base;

import com.jimmy.friday.boot.enums.EventTypeEnum;

import java.io.Serializable;

public interface Message extends Serializable {

    EventTypeEnum type();
}
