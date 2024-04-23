package com.jimmy.friday.protocol.registered;

import com.google.common.collect.Maps;
import com.jimmy.friday.protocol.annotations.RegisteredType;
import com.jimmy.friday.protocol.base.Input;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.core.DisruptorEvent;
import com.jimmy.friday.protocol.core.DisruptorEventFactory;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RegisteredType(type = ProtocolEnum.MEMORY)
public class MemoryRegistered extends BaseRegistered {

    private static final int RING_BUFFER_SIZE = 32 * 32;

    private Map<String, Input> inputMap = Maps.newHashMap();

    private Disruptor<DisruptorEvent> disruptor;

    @Override
    public void init(ProtocolProperty protocolProperty) {
        disruptor = new Disruptor<>(new DisruptorEventFactory(), RING_BUFFER_SIZE, new ThreadFactory() {
            final AtomicLong count = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("hawk-eye" + count.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        }, ProducerType.MULTI, new YieldingWaitStrategy());
        disruptor.setDefaultExceptionHandler(new ExceptionHandler() {
            @Override
            public void handleEventException(Throwable ex, long sequence, Object event) {
                log.error("事件处理失败,data:{}", event);
            }

            @Override
            public void handleOnStartException(Throwable throwable) {
                log.error("disruptor启动失败", throwable);
            }

            @Override
            public void handleOnShutdownException(Throwable throwable) {
                log.error("disruptorg关闭失败", throwable);
            }
        });

        disruptor.handleEventsWithWorkerPool(event -> {
            Input input = inputMap.get(event.getTopic());
            if (input != null) {
                try {
                    input.invoke(event.getData());
                } catch (Exception e) {
                    log.error("内存队列消费失败", e);
                }
            }
        });

        disruptor.start();
    }

    @Override
    public void close(Protocol info) {
        if (disruptor != null) {
            disruptor.shutdown();
            disruptor = null;
        }
    }

    @Override
    public void close() {
        if (disruptor != null) {
            disruptor.shutdown();
            disruptor = null;
        }
    }

    @Override
    public Input inputGene(Protocol info, Input input) {
        this.inputMap.put(info.getTopic(), input);
        return input;
    }

    @Override
    public Output outputGene(Protocol info) {
        return message -> {
            RingBuffer<DisruptorEvent> ringBuffer = disruptor.getRingBuffer();
            long sequence = ringBuffer.next();
            DisruptorEvent event = ringBuffer.get(sequence);

            event.setData(message);
            event.setTopic(info.getTopic());
            ringBuffer.publish(sequence);
            return null;
        };
    }
}
