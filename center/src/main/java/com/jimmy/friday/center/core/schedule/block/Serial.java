package com.jimmy.friday.center.core.schedule.block;

import cn.hutool.core.thread.ThreadUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.center.Schedule;
import com.jimmy.friday.center.base.schedule.Block;
import com.jimmy.friday.center.entity.ScheduleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class Serial implements Block {

    private final ConcurrentMap<Long, Thread> threadMap = Maps.newConcurrentMap();

    private final ConcurrentMap<Long, Queue<ScheduleJob>> queueMap = Maps.newConcurrentMap();

    @Autowired
    private Schedule schedule;

    @Override
    public void block(ScheduleJob scheduleJob) {
        Long id = scheduleJob.getId();
        //放入队列
        queueMap.computeIfAbsent(id, i -> new ConcurrentLinkedQueue<>());
        queueMap.get(id).add(scheduleJob.clear());

        if (!threadMap.containsKey(id)) {
            Thread thread = new Thread(() -> {
                while (true) {
                    Queue<ScheduleJob> queue = queueMap.get(id);
                    if (queue == null) {
                        break;
                    }

                    ScheduleJob poll = queue.poll();
                    if (poll == null) {
                        ThreadUtil.sleep(100);
                        continue;
                    }

                    schedule.submit(poll);
                }
            });

            Thread put = threadMap.put(id, thread);
            if (put == null) {
                thread.start();
            }
        }
    }

    @Override
    public BlockHandlerStrategyTypeEnum type() {
        return BlockHandlerStrategyTypeEnum.SERIAL;
    }
}
