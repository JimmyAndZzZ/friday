package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.JobRunStatusEnum;
import com.jimmy.friday.center.dao.ScheduleJobLogDao;
import com.jimmy.friday.center.entity.ScheduleJobLog;
import com.jimmy.friday.center.service.ScheduleJobLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (ScheduleJobLog)表服务实现类
 *
 * @author makejava
 * @since 2024-05-06 15:10:28
 */
@Service("scheduleJobLogService")
public class ScheduleJobLogServiceImpl extends ServiceImpl<ScheduleJobLogDao, ScheduleJobLog> implements ScheduleJobLogService {

    @Override
    public boolean fail(ScheduleJobLog scheduleJobLog) {
        return baseMapper.fail(scheduleJobLog.getRunStatus(), scheduleJobLog.getEndDate(), scheduleJobLog.getErrorMessage(), scheduleJobLog.getId());
    }

    @Override
    public ScheduleJobLog queryByTraceId(Long traceId) {
        QueryWrapper<ScheduleJobLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("trace_id", traceId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ScheduleJobLog> queryTimeout() {
        QueryWrapper<ScheduleJobLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("run_status", JobRunStatusEnum.RUNNING.getCode());
        queryWrapper.lt("timeout_date", System.currentTimeMillis());
        return this.list(queryWrapper);
    }
}

