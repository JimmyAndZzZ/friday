package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.ScheduleJobLog;

import java.util.List;

/**
 * (ScheduleJobLog)表服务接口
 *
 * @author makejava
 * @since 2024-05-06 15:10:28
 */
public interface ScheduleJobLogService extends IService<ScheduleJobLog> {

    List<ScheduleJobLog> queryNotFinish(Long executorId);

    List<ScheduleJobLog> queryNoTimeout();

    List<ScheduleJobLog> queryTimeout();

    ScheduleJobLog queryByTraceId(Long traceId);

    boolean fail(ScheduleJobLog scheduleJobLog);
}

