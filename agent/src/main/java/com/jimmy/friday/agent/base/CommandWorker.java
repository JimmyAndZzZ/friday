package com.jimmy.friday.agent.base;

import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.enums.CommandTypeEnum;

public interface CommandWorker<T> {

    void open(Command command);

    void process(T t);

    void finish();

    CommandTypeEnum command();
}
