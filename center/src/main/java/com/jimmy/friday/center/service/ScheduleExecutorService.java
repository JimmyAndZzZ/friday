package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.ScheduleExecutor;

/**
 * (ScheduleExecutor)表服务接口
 *
 * @author makejava
 * @since 2024-04-28 15:32:14
 */
public interface ScheduleExecutorService extends IService<ScheduleExecutor> {

    ScheduleExecutor query(String applicationName, String ip);

    void register(String applicationName, String ip);

    void offline(String applicationName, String ip);

}

