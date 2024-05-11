package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.enums.schedule.ScheduleRunStatusEnum;
import com.jimmy.friday.center.entity.ScheduleJobLog;

import java.util.Date;
import java.util.List;

/**
 * (ScheduleJobLog)表服务接口
 *
 * @author makejava
 * @since 2024-05-06 15:10:28
 */
public interface ScheduleJobLogService extends IService<ScheduleJobLog> {

    IPage<ScheduleJobLog> page(Date startDate,
                               Date endDate,
                               Long jobId,
                               ScheduleRunStatusEnum scheduleRunStatusEnum,
                               Integer pageNo,
                               Integer pageSize);

    List<ScheduleJobLog> queryNotFinish(Long executorId);

    List<ScheduleJobLog> queryNoTimeout();

    List<ScheduleJobLog> queryTimeout();

    ScheduleJobLog queryByTraceId(Long traceId);

    boolean fail(ScheduleJobLog scheduleJobLog);
}

