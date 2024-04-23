package com.jimmy.friday.center.base;

import com.jimmy.friday.protocol.core.InputMessage;

public interface Process {

    void process(InputMessage message) throws Exception;
}
