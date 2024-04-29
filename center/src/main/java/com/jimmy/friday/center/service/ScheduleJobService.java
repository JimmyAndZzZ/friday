package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.ScheduleJob;

import java.util.List;

/**
 * (ScheduleJob)表服务接口
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
public interface ScheduleJobService extends IService<ScheduleJob> {

    void updateNextExecuteTime(Long nextTime, Integer id);

    void removeByApplicationName(String applicationName);

    List<ScheduleJob> queryByApplicationName(String applicationName);

    ScheduleJob queryByCodeAndApplicationName(String code, String applicationName);

    void updateExecuteTime(Long lastTime, Long nextTime, Integer id);

    void updateStatus(String status, Integer id);

    boolean needExecute(Integer id, Long nextTime);

    List<ScheduleJob> queryExecuteJobs(Long time, Integer size);
}

