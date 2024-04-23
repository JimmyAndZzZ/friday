package com.jimmy.friday.protocol.core;

import com.jimmy.friday.protocol.base.Input;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
@Data
public class InputProxy implements Input {

    private Object o;

    private Method m;

    @Override
    public Object invoke(String message) throws Exception {
        return m.invoke(o, message);
    }
}
