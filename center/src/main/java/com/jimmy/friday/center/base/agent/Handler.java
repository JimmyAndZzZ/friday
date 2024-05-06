package com.jimmy.friday.center.base.agent;

import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.enums.CommandTypeEnum;

public interface Handler {

    String execute(Command command, String sessionKey);

    CommandTypeEnum type();

    boolean isCover();
}
