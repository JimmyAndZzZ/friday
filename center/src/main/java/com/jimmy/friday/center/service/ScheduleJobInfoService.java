package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.ScheduleJobInfo;

import java.util.List;

/**
 * (ScheduleJobInfo)表服务接口
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
public interface ScheduleJobInfoService extends IService<ScheduleJobInfo> {

    void updateExecuteTime(Long lastTime, Long nextTime, Integer id);

    void updateStatus(String status, Integer id);

    boolean needExecute(Integer id, Long nextTime);

    List<ScheduleJobInfo> queryExecuteJobs(Long time, Integer size);
}

