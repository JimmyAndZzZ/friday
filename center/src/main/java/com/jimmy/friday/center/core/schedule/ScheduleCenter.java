package com.jimmy.friday.center.core.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.enums.ScheduleStatusEnum;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.ScheduleJob;
import com.jimmy.friday.center.other.CronExpression;
import com.jimmy.friday.center.service.ScheduleJobService;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ScheduleCenter implements Initialize {

    private static final int READ_COUNT = 200;

    private static final long PRE_READ_MS = 5000;

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private ScheduleTimeRing scheduleTimeRing;

    @Autowired
    private ScheduleExecutePool scheduleExecutePool;

    @Autowired
    private ScheduleJobService scheduleJobService;

    @Override
    public void init() throws Exception {
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
                        Integer id = scheduleJobInfo.getId();
                        Long nextTime = scheduleJobInfo.getNextTime();
                        String redisKey = RedisConstants.Schedule.SCHEDULE_EXECUTE_JOB_LOCK + id + ":" + nextTime;

                        if (stripedLock.tryLock(redisKey, 60L, TimeUnit.SECONDS)) {
                            try {
                                if (scheduleJobService.needExecute(id, nextTime)) {
                                    // 超过轮训周期
                                    if (nowTime > nextTime + PRE_READ_MS) {
                                        scheduleExecutePool.execute(scheduleJobInfo);

                                        this.updateScheduleJobInfo(scheduleJobInfo, System.currentTimeMillis());
                                    } else if (nowTime > nextTime) {
                                        scheduleExecutePool.execute(scheduleJobInfo);

                                        this.updateScheduleJobInfo(scheduleJobInfo, System.currentTimeMillis());
                                        //时间范围内触发直接丢时间轮
                                        while (ScheduleStatusEnum.OPEN.getCode().equals(scheduleJobInfo.getStatus()) && nowTime + PRE_READ_MS > scheduleJobInfo.getNextTime()) {
                                            scheduleTimeRing.push(scheduleJobInfo);

                                            this.updateScheduleJobInfo(scheduleJobInfo, scheduleJobInfo.getNextTime());
                                        }
                                    } else {
                                        scheduleTimeRing.push(scheduleJobInfo);

                                        this.updateScheduleJobInfo(scheduleJobInfo, System.currentTimeMillis());
                                    }
                                }
                            } finally {
                                stripedLock.releaseLock(redisKey);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("定时器扫描失败", e);
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public int sort() {
        return 1;
    }


    /**
     * 刷新定时器信息
     *
     * @param scheduleJob
     */
    private void updateScheduleJobInfo(ScheduleJob scheduleJob, Long lastTime) {
        Integer id = scheduleJob.getId();
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

            scheduleJobService.updateExecuteTime(scheduleJob.getNextTime(), nextTime, id);
        } else {
            log.error("定时器:{},获取下次执行时间失败", id);

            scheduleJob.setStatus(ScheduleStatusEnum.CLOSE.getCode());

            scheduleJobService.updateStatus(ScheduleStatusEnum.CLOSE.getCode(), id);
        }
    }

    /**
     * 获取下次执行时间
     *
     * @param cron
     * @param lastTime
     * @return
     * @throws Exception
     */
    private Long generateNextTime(String cron, Long lastTime) {
        try {
            Date nextValidTimeAfter = new CronExpression(cron).getNextValidTimeAfter(new Date(lastTime));
            return nextValidTimeAfter != null ? nextValidTimeAfter.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }
}
