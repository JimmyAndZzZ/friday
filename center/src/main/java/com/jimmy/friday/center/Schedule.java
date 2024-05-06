package com.jimmy.friday.center;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;
import com.jimmy.friday.boot.message.schedule.ScheduleInvoke;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.entity.ScheduleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class Schedule {

    private final Map<Long, ScheduleResult> result = Maps.newConcurrentMap();

    private final Map<Long, CountDownLatch> confirm = Maps.newConcurrentMap();

    @Autowired
    private AttachmentCache attachmentCache;

    public void callback() {

    }

    public boolean isRunning(Long id) {
        return false;
    }

    public ScheduleResult submit(ScheduleJob scheduleJob) {
        Long traceId = IdUtil.getSnowflake(1, 1).nextId();


        return null;
    }

    /**
     * 调用定时器
     *
     * @param scheduleInvoke
     */
    private void invoke(ScheduleInvoke scheduleInvoke) {

    }
}
