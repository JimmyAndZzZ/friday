package com.jimmy.friday.agent.plugin.action.construct;

import com.jimmy.friday.agent.base.ConstructInterceptorAction;
import lombok.Getter;
import lombok.Setter;

public abstract class BaseConstructInterceptorAction implements ConstructInterceptorAction {

    @Getter
    @Setter
    private String className;
}
