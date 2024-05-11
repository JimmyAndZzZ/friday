package com.jimmy.friday.center;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.schedule.ScheduleExecutor;
import com.jimmy.friday.boot.enums.schedule.ScheduleRunStatusEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleInterrupt;
import com.jimmy.friday.boot.message.schedule.ScheduleInvoke;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.schedule.ScheduleSession;
import com.jimmy.friday.center.entity.ScheduleJob;
import com.jimmy.friday.center.entity.ScheduleJobLog;
import com.jimmy.friday.center.service.ScheduleJobLogService;
import com.jimmy.friday.center.service.ScheduleJobService;
import com.jimmy.friday.center.support.TransmitSupport;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
    private ScheduleJobService scheduleJobService;

    @Autowired
    private ScheduleJobLogService scheduleJobLogService;

    public void interrupt(ScheduleJobLog scheduleJobLog) {
        Long jobId = scheduleJobLog.getJobId();
        Long traceId = scheduleJobLog.getTraceId();

        log.info("准备中断定时器调度,jobId:{},traceId:{}", jobId, traceId);

        String applicationIdByExecutorId = scheduleSession.getApplicationIdByExecutorId(scheduleJobLog.getExecutorId());
        if (applicationIdByExecutorId == null) {
            log.error("调度执行器未连接,{}", scheduleJobLog.getExecutorId());
            this.callback(traceId, System.currentTimeMillis(), false, "调度被中断,原因:执行器离线");
            return;
        }

        this.callback(traceId, System.currentTimeMillis(), false, "调度被中断,原因:外部中断");

        ScheduleInterrupt scheduleInterrupt = new ScheduleInterrupt();
        scheduleInterrupt.setScheduleId(scheduleJobLog.getJobCode());
        scheduleInterrupt.setTraceId(traceId);
        transmitSupport.transmit(scheduleInterrupt, applicationIdByExecutorId);
    }

    public void callback(Long traceId, Long endDate, Boolean isSuccess, String errorMessage) {
        ScheduleJobLog scheduleJobLog = scheduleJobLogService.queryByTraceId(traceId);
        if (scheduleJobLog == null) {
            log.error("调度运行日志不存在,{}", traceId);
            return;
        }

        Long jobId = scheduleJobLog.getJobId();
        Integer currentShardingNum = scheduleJobLog.getCurrentShardingNum();

        ScheduleJob byId = scheduleJobService.getById(jobId);
        if (byId == null) {
            log.error("定时器不存在,{}", jobId);

            scheduleJobLog.setRunStatus(ScheduleRunStatusEnum.ERROR.getCode());
            scheduleJobLog.setErrorMessage("调度被中断,原因:定时器不存在");
            scheduleJobLog.setEndDate(endDate);
            scheduleJobLogService.fail(scheduleJobLog);
            return;
        }

        String redisKey = RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + jobId;
        if (currentShardingNum != null) {
            redisKey = redisKey + ":" + currentShardingNum;
        }

        attachmentCache.remove(redisKey);

        if (!ScheduleRunStatusEnum.RUNNING.getCode().equals(scheduleJobLog.getRunStatus())) {
            log.error("调度结束，更新无效,{}", traceId);
            return;
        }

        if (isSuccess) {
            scheduleJobLog.setRunStatus(ScheduleRunStatusEnum.SUCCESS.getCode());
            scheduleJobLog.setEndDate(endDate);
            scheduleJobLogService.updateById(scheduleJobLog);
        } else {
            scheduleJobLog.setRunStatus(ScheduleRunStatusEnum.ERROR.getCode());
            scheduleJobLog.setErrorMessage(errorMessage);
            scheduleJobLog.setEndDate(endDate);
            scheduleJobLogService.fail(scheduleJobLog);
        }
    }

    public boolean isRunning(Long id, Integer shardingNum) {
        String redisKey = RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + id;
        if (shardingNum != null) {
            redisKey = redisKey + ":" + shardingNum;
        }

        return StrUtil.isNotEmpty(attachmentCache.attachment(redisKey));
    }

    public void release(Long id) {
        attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_SHARDING_NUM + id);
        attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + id);
    }

    public void checkRunning() {
        Iterable<String> keys = attachmentCache.keys(RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + "*");
        if (CollUtil.isNotEmpty(keys)) {
            for (String key : keys) {
                String attachment = attachmentCache.attachment(key);
                if (StrUtil.isEmpty(attachment)) {
                    continue;
                }

                ScheduleJobLog scheduleJobLog = scheduleJobLogService.queryByTraceId(Convert.toLong(attachment, -1L));
                if (scheduleJobLog == null || !ScheduleRunStatusEnum.RUNNING.getCode().equals(scheduleJobLog.getRunStatus())) {
                    attachmentCache.remove(key);
                }
            }
        }
    }

    public void submit(ScheduleJob scheduleJob, Integer currentShardingNum) {
        Long id = scheduleJob.getId();
        Long timeout = scheduleJob.getTimeout();
        String runParam = scheduleJob.getRunParam();
        Integer shardingNum = scheduleJob.getShardingNum();
        String applicationName = scheduleJob.getApplicationName();
        Long traceId = IdUtil.getSnowflake(1, 1).nextId();

        log.info("准备执行定时器,id:{},code:{},applicationName:{}", id, scheduleJob.getCode(), applicationName);
        //服务
        ScheduleExecutor select = scheduleSession.select(applicationName, Sets.newHashSet());
        if (select == null) {
            log.error("执行引擎为空,id:{},code:{},applicationName:{}", id, scheduleJob.getCode(), applicationName);
            return;
        }
        //保存运行流水
        ScheduleJobLog scheduleJobLog = new ScheduleJobLog();
        scheduleJobLog.setJobId(id);
        scheduleJobLog.setRunParam(runParam);
        scheduleJobLog.setExecutorId(select.getId());
        scheduleJobLog.setStartDate(System.currentTimeMillis());
        scheduleJobLog.setRunStatus(ScheduleRunStatusEnum.RUNNING.getCode());
        scheduleJobLog.setJobCode(scheduleJob.getCode());
        scheduleJobLog.setTraceId(traceId);
        scheduleJobLog.setShardingNum(shardingNum);
        scheduleJobLog.setCurrentShardingNum(currentShardingNum != null ? currentShardingNum : 0);
        //计算超时时间
        if (timeout != null && timeout > 0) {
            scheduleJobLog.setTimeoutDate(System.currentTimeMillis() + timeout * 1000);
        }

        scheduleJobLogService.save(scheduleJobLog);

        ScheduleInvoke invoke = new ScheduleInvoke();
        invoke.setParam(runParam);
        invoke.setTraceId(traceId);
        invoke.setTimeout(invoke.getTimeout());
        invoke.setScheduleId(scheduleJob.getCode());
        invoke.setRetry(scheduleJob.getRetryCount());

        String redisKey = RedisConstants.Schedule.SCHEDULE_JOB_RUNNING_FLAG + id;
        if (currentShardingNum != null) {
            redisKey = redisKey + ":" + currentShardingNum;
        }

        try {
            attachmentCache.attachString(redisKey, traceId.toString(), timeout, TimeUnit.SECONDS);

            this.invoke(invoke, select.getApplicationId());
        } catch (Exception e) {
            log.error("调度执行失败:{},code:{},applicationName:{}", id, scheduleJob.getCode(), applicationName, e);

            scheduleJobLog.setRunStatus(ScheduleRunStatusEnum.ERROR.getCode());
            scheduleJobLog.setErrorMessage(e.getMessage());
            scheduleJobLogService.updateById(scheduleJobLog);

            attachmentCache.remove(redisKey);
        }
    }

    public void invoke(ScheduleInvoke scheduleInvoke, String applicationId) {
        transmitSupport.transmit(scheduleInvoke, applicationId);
    }
}
