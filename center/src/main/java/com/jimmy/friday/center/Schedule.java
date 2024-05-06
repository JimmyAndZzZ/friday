package com.jimmy.friday.center;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.schedule.ScheduleExecutor;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;
import com.jimmy.friday.boot.enums.JobRunStatusEnum;
import com.jimmy.friday.boot.enums.ScheduleStatusEnum;
import com.jimmy.friday.boot.exception.ScheduleException;
import com.jimmy.friday.boot.message.schedule.ScheduleInvoke;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.schedule.ScheduleSession;
import com.jimmy.friday.center.entity.ScheduleJob;
import com.jimmy.friday.center.entity.ScheduleJobLog;
import com.jimmy.friday.center.service.ScheduleJobLogService;
import com.jimmy.friday.center.support.TransmitSupport;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Schedule {

    @Autowired
    private ScheduleSession scheduleSession;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private TransmitSupport transmitSupport;

    @Autowired
    private ScheduleJobLogService scheduleJobLogService;

    public void callback(ScheduleResult scheduleResult) {
        ScheduleJobLog scheduleJobLog = scheduleJobLogService.queryByTraceId(scheduleResult.getTraceId());
        if (scheduleJobLog == null) {
            log.error("调度运行日志不存在,{}", scheduleResult.getTraceId());
            return;
        }

        if (!JobRunStatusEnum.RUNNING.getCode().equals(scheduleJobLog.getRunStatus())) {
            log.error("调度结束，更新无效,{}", scheduleResult.getTraceId());
            return;
        }

        Boolean isSuccess = scheduleResult.getIsSuccess();
        if (isSuccess) {
            scheduleJobLog.setRunStatus(JobRunStatusEnum.SUCCESS.getCode());
        } else {
            scheduleJobLog.setRunStatus(JobRunStatusEnum.ERROR.getCode());
            scheduleJobLog.setErrorMessage(scheduleResult.getErrorMessage());
        }

        scheduleJobLog.setEndDate(scheduleResult.getEndDate());
        scheduleJobLogService.updateById(scheduleJobLog);
    }

    public boolean isRunning(Long id) {
        return StrUtil.isNotEmpty(attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + id));
    }

    public void submit(ScheduleJob scheduleJob) {
        Long id = scheduleJob.getId();
        String runParam = scheduleJob.getRunParam();
        String applicationName = scheduleJob.getApplicationName();
        Long traceId = IdUtil.getSnowflake(1, 1).nextId();
        //服务
        ScheduleExecutor select = scheduleSession.select(applicationName, Sets.newHashSet());
        if (select == null) {
            log.error("执行引擎为空,applicationName:{}", applicationName);
            return;
        }
        //保存运行流水
        ScheduleJobLog scheduleJobLog = new ScheduleJobLog();
        scheduleJobLog.setJobId(id);
        scheduleJobLog.setTimeout(scheduleJob.getTimeout());
        scheduleJobLog.setRunParam(runParam);
        scheduleJobLog.setExecutorId(select.getId());
        scheduleJobLog.setStartDate(System.currentTimeMillis());
        scheduleJobLog.setRunStatus(JobRunStatusEnum.RUNNING.getCode());
        scheduleJobLog.setTraceId(traceId);
        scheduleJobLogService.save(scheduleJobLog);

        ScheduleInvoke invoke = new ScheduleInvoke();
        invoke.setScheduleId(scheduleJob.getCode());
        invoke.setTraceId(traceId);
        invoke.setParam(runParam);

        try {
            attachmentCache.attachString(RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + id, traceId.toString(), scheduleJob.getTimeout(), TimeUnit.SECONDS);

            this.invoke(invoke, select.getApplicationId());
        } catch (Exception e) {
            log.error("调度分配失败", e);

            scheduleJobLog.setRunStatus(JobRunStatusEnum.ERROR.getCode());
            scheduleJobLog.setErrorMessage(e.getMessage());
            scheduleJobLogService.updateById(scheduleJobLog);

            attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + id);
        }
    }

    /**
     * 调用定时器
     *
     * @param scheduleInvoke
     */
    private void invoke(ScheduleInvoke scheduleInvoke, String applicationId) {
        transmitSupport.transmit(scheduleInvoke, applicationId);
    }
}
