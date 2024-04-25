package com.jimmy.friday.center.core.schedule;

import com.jimmy.friday.center.base.Close;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ScheduleExecutePool implements Close {

    private final ThreadPoolExecutor highPool = new ThreadPoolExecutor(
            10,
            100,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "friday-schedule-high-" + r.hashCode()));

    private final ThreadPoolExecutor normalPool = new ThreadPoolExecutor(
            10,
            200,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            r -> new Thread(r, "friday-schedule-normal-" + r.hashCode()));

    private final ThreadPoolExecutor lowPool = new ThreadPoolExecutor(
            5,
            50,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            r -> new Thread(r, "friday-schedule-low-" + r.hashCode()));

    public void execute(String scheduleId) {

    }

    @Override
    public void close() {
        this.highPool.shutdown();
        this.normalPool.shutdown();
        this.lowPool.shutdown();
    }
}
