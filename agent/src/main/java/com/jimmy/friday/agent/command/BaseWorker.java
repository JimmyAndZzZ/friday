package com.jimmy.friday.agent.command;

import com.jimmy.friday.agent.base.CommandWorker;

import java.text.DecimalFormat;

public abstract class BaseWorker<T> implements CommandWorker<T> {

    @Override
    public void process(T t) {

    }

    @Override
    public void finish() {

    }

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
