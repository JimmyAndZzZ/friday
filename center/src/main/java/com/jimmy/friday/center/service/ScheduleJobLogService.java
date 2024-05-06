package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.ScheduleJobLog;

/**
 * (ScheduleJobLog)表服务接口
 *
 * @author makejava
 * @since 2024-05-06 15:10:28
 */
public interface ScheduleJobLogService extends IService<ScheduleJobLog> {

    ScheduleJobLog queryByTraceId(Long traceId);
}

