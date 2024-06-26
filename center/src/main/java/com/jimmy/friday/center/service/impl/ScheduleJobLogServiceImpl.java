package com.jimmy.friday.center.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.schedule.ScheduleRunStatusEnum;
import com.jimmy.friday.center.dao.ScheduleJobLogDao;
import com.jimmy.friday.center.entity.ScheduleJob;
import com.jimmy.friday.center.entity.ScheduleJobLog;
import com.jimmy.friday.center.service.ScheduleJobLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * (ScheduleJobLog)表服务实现类
 *
 * @author makejava
 * @since 2024-05-06 15:10:28
 */
@Service("scheduleJobLogService")
public class ScheduleJobLogServiceImpl extends ServiceImpl<ScheduleJobLogDao, ScheduleJobLog> implements ScheduleJobLogService {

    private static final int NOT_FINISH_TIMEOUT_DATE_DELAY = 5000;

    private static final int NO_TIMEOUT_START_DATE_DELAY = 30000;

    @Override
    public IPage<ScheduleJobLog> page(Date startDate,
                                      Date endDate,
                                      Long jobId,
                                      ScheduleRunStatusEnum scheduleRunStatusEnum,
                                      Integer pageNo,
                                      Integer pageSize) {

        QueryWrapper<ScheduleJobLog> queryWrapper = new QueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge("start_date", startDate.getTime());
        }

        if (endDate != null) {
            queryWrapper.le("start_date", endDate.getTime());
        }

        if (jobId != null) {
            queryWrapper.eq("job_id", jobId);
        }

        if (scheduleRunStatusEnum != null) {
            queryWrapper.eq("run_status", scheduleRunStatusEnum.getCode());
        }

        queryWrapper.orderByDesc("start_date");
        return this.page(new Page<>(pageNo, pageSize), queryWrapper);
    }

    @Override
    public List<ScheduleJobLog> queryNotFinish(Long executorId) {
        QueryWrapper<ScheduleJobLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("executor_id", executorId);
        queryWrapper.eq("run_status", ScheduleRunStatusEnum.RUNNING.getCode());
        queryWrapper.or(w -> w.isNull("timeout_date").or().ge("timeout_date", System.currentTimeMillis() + NOT_FINISH_TIMEOUT_DATE_DELAY));
        return this.list(queryWrapper);
    }

    @Override
    public List<ScheduleJobLog> queryNoTimeout() {
        QueryWrapper<ScheduleJobLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("run_status", ScheduleRunStatusEnum.RUNNING.getCode());
        queryWrapper.isNull("timeout_date");
        queryWrapper.le("start_date", System.currentTimeMillis() - NO_TIMEOUT_START_DATE_DELAY);
        return this.list(queryWrapper);
    }

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
        queryWrapper.eq("run_status", ScheduleRunStatusEnum.RUNNING.getCode());
        queryWrapper.lt("timeout_date", System.currentTimeMillis());
        return this.list(queryWrapper);
    }
}

