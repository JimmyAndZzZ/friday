package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.center.entity.HawkEyeLog;

import java.util.List;

/**
 * (HawkEyeLog)表服务接口
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
public interface HawkEyeLogService extends IService<HawkEyeLog> {

    void push(List<Trace> traces);
}

