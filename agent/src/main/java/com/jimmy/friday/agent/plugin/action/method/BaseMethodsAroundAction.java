package com.jimmy.friday.agent.plugin.action.method;

import com.jimmy.friday.agent.base.MethodsAroundAction;

import java.text.DecimalFormat;

public abstract class BaseMethodsAroundAction implements MethodsAroundAction {


    /**
     * 纳秒转毫秒
     *
     * @return
     */
    protected String naoToMs(Long cost) {
        double elapsedTime = cost / 1_000_000.0;
        DecimalFormat decimalFormat = new DecimalFormat("0.##########");
        return decimalFormat.format(elapsedTime);
    }
}
