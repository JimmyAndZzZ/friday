package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.ScheduleExecutor;

import java.util.List;

/**
 * (ScheduleExecutor)表服务接口
 *
 * @author makejava
 * @since 2024-04-28 15:32:14
 */
public interface ScheduleExecutorService extends IService<ScheduleExecutor> {

    List<String> getApplicationList();

    ScheduleExecutor query(String applicationName, String ip);

    ScheduleExecutor register(String applicationName, String ip);

    void offline(String applicationName, String ip);

}

