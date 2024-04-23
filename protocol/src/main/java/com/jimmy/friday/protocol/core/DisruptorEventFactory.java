package com.jimmy.friday.protocol.core;

import com.lmax.disruptor.EventFactory;

public class DisruptorEventFactory implements EventFactory<DisruptorEvent> {

    @Override
    public DisruptorEvent newInstance() {
        return new DisruptorEvent();
    }
}
