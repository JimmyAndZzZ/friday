package com.jimmy.friday.center.core.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.schedule.ScheduleExecutor;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.core.schedule.ScheduleRunInfo;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.boot.enums.schedule.ScheduleRunStatusEnum;
import com.jimmy.friday.boot.enums.schedule.ScheduleSourceEnum;
import com.jimmy.friday.boot.enums.schedule.ScheduleStatusEnum;
import com.jimmy.friday.center.Schedule;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.ScheduleJob;
import com.jimmy.friday.center.entity.ScheduleJobLog;
import com.jimmy.friday.center.other.CronExpression;
import com.jimmy.friday.center.service.ScheduleJobLogService;
import com.jimmy.friday.center.service.ScheduleJobService;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScheduleCenter implements Initialize {

    private static final int READ_COUNT = 200;

    private static final long PRE_READ_MS = 5000;

    @Autowired
    private Schedule schedule;

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private ScheduleSession scheduleSession;

    @Autowired
    private ScheduleTimeRing scheduleTimeRing;

    @Autowired
    private ScheduleJobService scheduleJobService;

    @Autowired
    private ScheduleExecutePool scheduleExecutePool;

    @Autowired
    private ScheduleJobLogService scheduleJobLogService;

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        //超时扫描
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> stripedLock.tryLock(RedisConstants.Schedule.SCHEDULE_NO_TIMEOUT_JOB_SCAN_LOCK, 300L, TimeUnit.SECONDS, () -> {
            try {
                List<ScheduleJobLog> scheduleJobLogs = scheduleJobLogService.queryNoTimeout();
                if (CollUtil.isNotEmpty(scheduleJobLogs)) {
                    Map<Long, List<ScheduleJobLog>> groupBy = scheduleJobLogs.stream().collect(Collectors.groupingBy(ScheduleJobLog::getExecutorId));

                    for (Map.Entry<Long, List<ScheduleJobLog>> entry : groupBy.entrySet()) {
                        Long key = entry.getKey();
                        List<ScheduleJobLog> value = entry.getValue();

                        String applicationIdByExecutorId = scheduleSession.getApplicationIdByExecutorId(key);
                        for (ScheduleJobLog scheduleJobLog : value) {
                            Long traceId = scheduleJobLog.getTraceId();

                            if (StrUtil.isEmpty(applicationIdByExecutorId)) {
                                schedule.callback(traceId, System.currentTimeMillis(), false, "调度被中断,原因:执行器离线");
                                continue;
                            }

                            List<ScheduleRunInfo> realTimeRunInfo = scheduleSession.getRealTimeRunInfo(applicationIdByExecutorId);
                            Set<Long> traceIds = CollUtil.isEmpty(realTimeRunInfo) ? Sets.newHashSet() : realTimeRunInfo.stream().map(ScheduleRunInfo::getTraceId).collect(Collectors.toSet());

                            if (!traceIds.contains(traceId)) {
                                schedule.callback(traceId, System.currentTimeMillis(), false, "调度被中断,原因:进程消失");
                            }
                        }
                    }
                }
                //校验运行状态
                schedule.checkRunning();
            } catch (Exception e) {
                log.error("校验运行状态定时器运行失败", e);
            }
        }), 0, 60, TimeUnit.SECONDS);
        //超时扫描
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> stripedLock.tryLock(RedisConstants.Schedule.SCHEDULE_TIMEOUT_JOB_LOCK, 300L, TimeUnit.SECONDS, () -> {
            try {
                List<ScheduleJobLog> scheduleJobLogs = scheduleJobLogService.queryTimeout();
                if (CollUtil.isNotEmpty(scheduleJobLogs)) {
                    for (ScheduleJobLog scheduleJobLog : scheduleJobLogs) {
                        scheduleJobLog.setEndDate(System.currentTimeMillis());
                        scheduleJobLog.setRunStatus(ScheduleRunStatusEnum.TIMEOUT.getCode());
                        scheduleJobLog.setErrorMessage("运行超时");
                        //乐观锁
                        if (scheduleJobLogService.fail(scheduleJobLog)) {
                            schedule.interrupt(scheduleJobLog);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("调度超时定时器运行失败", e);
            }
        }), 0, 30, TimeUnit.SECONDS);
        //扫描定时器
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    //整秒休眠
                    ThreadUtil.sleep(PRE_READ_MS - System.currentTimeMillis() % 1000);

                    long nowTime = System.currentTimeMillis();

                    List<ScheduleJob> scheduleJobs = scheduleJobService.queryExecuteJobs(nowTime + PRE_READ_MS, READ_COUNT);
                    if (CollUtil.isEmpty(scheduleJobs)) {
                        continue;
                    }

                    for (ScheduleJob scheduleJobInfo : scheduleJobs) {
                        Long id = scheduleJobInfo.getId();
                        Long nextTime = scheduleJobInfo.getNextTime();
                        String redisKey = RedisConstants.Schedule.SCHEDULE_EXECUTE_JOB_LOCK + id + ":" + nextTime;

                        stripedLock.tryLock(redisKey, 60L, TimeUnit.SECONDS, () -> {
                            if (scheduleJobService.needExecute(id, nextTime)) {
                                // 超过轮训周期
                                if (nowTime > nextTime + PRE_READ_MS) {
                                    this.refreshScheduleNextTime(scheduleJobInfo, System.currentTimeMillis());
                                } else if (nowTime > nextTime) {
                                    scheduleExecutePool.execute(scheduleJobInfo);

                                    this.refreshScheduleNextTime(scheduleJobInfo, System.currentTimeMillis());
                                    //时间范围内触发直接丢时间轮
                                    while (ScheduleStatusEnum.OPEN.getCode().equals(scheduleJobInfo.getStatus()) && nowTime + PRE_READ_MS > scheduleJobInfo.getNextTime()) {
                                        scheduleTimeRing.push(scheduleJobInfo);

                                        this.refreshScheduleNextTime(scheduleJobInfo, scheduleJobInfo.getNextTime());
                                    }
                                } else {
                                    scheduleTimeRing.push(scheduleJobInfo);

                                    this.refreshScheduleNextTime(scheduleJobInfo, nextTime);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("定时器扫描失败", e);
                }
            }
        });

        thread.setName("schedule-scan-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public int sort() {
        return 1;
    }

    public void append(ScheduleInfo scheduleInfo, String applicationName) {
        ScheduleJob save = scheduleJobService.save(scheduleInfo, applicationName);
        if (save == null) {
            return;
        }

        this.updateSchedule(save, scheduleInfo);
    }

    public void delete(String scheduleId, String applicationName) {
        ScheduleJob scheduleJob = scheduleJobService.removeByCodeAndApplicationName(scheduleId, applicationName);
        if (scheduleJob != null) {
            scheduleExecutePool.release(scheduleJob.getId(), scheduleJob.getBlockStrategy());
            schedule.release(scheduleJob.getId());
        }
    }

    public void register(ScheduleExecutor connect, Collection<ScheduleInfo> scheduleInfos, String applicationName, String applicationId) {
        stripedLock.tryLock(RedisConstants.Schedule.SCHEDULE_JOB_RELOAD_LOCK + applicationId, 60L, TimeUnit.SECONDS, () -> {
            List<ScheduleJobLog> scheduleJobLogs = scheduleJobLogService.queryNotFinish(connect.getId());
            if (CollUtil.isNotEmpty(scheduleJobLogs)) {
                for (ScheduleJobLog scheduleJobLog : scheduleJobLogs) {
                    schedule.callback(scheduleJobLog.getTraceId(), System.currentTimeMillis(), false, "调度被中断,原因:执行器重启");
                }
            }
        });

        stripedLock.tryLock(RedisConstants.Schedule.SCHEDULE_REGISTER_JOB_LOCK + applicationId, 60L, TimeUnit.SECONDS, () -> {
            if (CollUtil.isEmpty(scheduleInfos)) {
                scheduleJobService.removeByApplicationName(applicationName, ScheduleSourceEnum.ANNOTATION);
                return;
            }
            //之前已存在的
            List<ScheduleJob> scheduleJobs = scheduleJobService.queryByApplicationName(applicationName, ScheduleSourceEnum.ANNOTATION);
            Map<String, ScheduleJob> map = CollUtil.isEmpty(scheduleJobs) ? Maps.newHashMap() : scheduleJobs.stream().collect(Collectors.toMap(ScheduleJob::getCode, g -> g));

            for (ScheduleInfo scheduleInfo : scheduleInfos) {
                ScheduleJob scheduleJob = map.remove(scheduleInfo.getScheduleId());
                if (scheduleJob != null) {
                    this.updateSchedule(scheduleJob, scheduleInfo);
                } else {
                    this.append(scheduleInfo, applicationName);
                }
            }

            if (MapUtil.isNotEmpty(map)) {
                scheduleJobService.removeByIds(map.values().stream().map(ScheduleJob::getId).collect(Collectors.toSet()));
            }
        });
    }

    public Long generateNextTime(String cron, Long lastTime) {
        try {
            Date nextValidTimeAfter = new CronExpression(cron).getNextValidTimeAfter(new Date(lastTime));
            return nextValidTimeAfter != null ? nextValidTimeAfter.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 更新调度信息
     *
     * @param scheduleJob
     * @param scheduleInfo
     */
    private void updateSchedule(ScheduleJob scheduleJob, ScheduleInfo scheduleInfo) {
        Long id = scheduleJob.getId();
        String cron = scheduleInfo.getCron();
        String blockStrategy = scheduleJob.getBlockStrategy();
        BlockHandlerStrategyTypeEnum blockHandlerStrategyType = scheduleInfo.getBlockHandlerStrategyType();

        if (ScheduleStatusEnum.OPEN.getCode().equals(scheduleJob.getStatus()) && !cron.equals(scheduleJob.getCron()) && !YesOrNoEnum.YES.getCode().equals(scheduleJob.getIsManual())) {
            Long lastTime = scheduleJob.getLastTime();
            scheduleJobService.updateNextExecuteTimeAndCron(this.generateNextTime(cron, lastTime != null ? lastTime : System.currentTimeMillis()), cron, id);
            //判断是否更新阻塞策略
            if (!blockHandlerStrategyType.getCode().equals(blockStrategy)) {
                if (scheduleJobService.updateBlockHandlerStrategyType(id, blockStrategy, blockHandlerStrategyType.getCode())) {
                    scheduleExecutePool.release(id, blockStrategy);
                }
            }
        }
    }


    /**
     * 刷新定时器信息
     *
     * @param scheduleJob
     */
    private void refreshScheduleNextTime(ScheduleJob scheduleJob, Long lastTime) {
        Long id = scheduleJob.getId();
        String cron = scheduleJob.getCron();
        if (StrUtil.isEmpty(cron)) {
            scheduleJob.setStatus(ScheduleStatusEnum.CLOSE.getCode());

            log.error("定时器:{},cron表达式为空", scheduleJob.getId());
            scheduleJobService.updateStatus(ScheduleStatusEnum.CLOSE.getCode(), id);
            return;
        }

        Long nextTime = this.generateNextTime(cron, lastTime);
        if (nextTime != null) {
            scheduleJob.setLastTime(scheduleJob.getNextTime());
            scheduleJob.setNextTime(nextTime);

            scheduleJobService.updateExecuteTime(scheduleJob.getLastTime(), nextTime, id);
        } else {
            log.error("定时器:{},获取下次执行时间失败", id);

            scheduleJob.setStatus(ScheduleStatusEnum.CLOSE.getCode());

            scheduleJobService.updateStatus(ScheduleStatusEnum.CLOSE.getCode(), id);
        }
    }
}
