package com.jimmy.friday.center.core.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.jimmy.friday.boot.enums.ScheduleStatusEnum;
import com.jimmy.friday.center.base.Close;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.ScheduleJobInfo;
import com.jimmy.friday.center.other.CronExpression;
import com.jimmy.friday.center.service.ScheduleJobInfoService;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ScheduleCenter implements Initialize, Close {

    private static final int READ_COUNT = 200;

    private static final long PRE_READ_MS = 5000;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private ScheduleTimeRing scheduleTimeRing;

    @Autowired
    private ScheduleExecutePool scheduleExecutePool;

    @Autowired
    private ScheduleJobInfoService scheduleJobInfoService;

    @Override
    public void init() throws Exception {
        executor.submit((Runnable) () -> {
            while (true) {
                try {
                    //整秒休眠
                    ThreadUtil.sleep(PRE_READ_MS - System.currentTimeMillis() % 1000);

                    long nowTime = System.currentTimeMillis();

                    List<ScheduleJobInfo> scheduleJobInfos = scheduleJobInfoService.queryExecuteJobs(nowTime + PRE_READ_MS, READ_COUNT);
                    if (CollUtil.isEmpty(scheduleJobInfos)) {
                        continue;
                    }

                    for (ScheduleJobInfo scheduleJobInfo : scheduleJobInfos) {
                        Integer id = scheduleJobInfo.getId();
                        String cron = scheduleJobInfo.getCron();
                        Long nextTime = scheduleJobInfo.getNextTime();
                        String redisKey = RedisConstants.Schedule.SCHEDULE_EXECUTE_JOB_LOCK + id + ":" + nextTime;

                        if (stripedLock.tryLock(redisKey, 60L, TimeUnit.SECONDS)) {
                            try {
                                if (scheduleJobInfoService.needExecute(id, nextTime)) {
                                    // 超过轮训周期
                                    if (nowTime > nextTime + PRE_READ_MS) {
                                        //todo
                                    }
                                } else if (nowTime > nextTime) {
                                    //todo
                                    Long next = this.getNextTimeCatchException(cron, id);
                                    if (next == null) {
                                        continue;
                                    }
                                    //时间范围内触发直接丢时间轮
                                    if (nowTime + PRE_READ_MS > next) {
                                        //todo

                                    }
                                } else {
                                    
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
    }

    @Override
    public int sort() {
        return 1;
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    /**
     * 获取下次执行时间
     *
     * @param cron
     * @param id
     * @return
     */
    private Long getNextTimeCatchException(String cron, Integer id) {
        try {
            Date next = this.generateNextTime(cron, System.currentTimeMillis());
            if (next != null) {
                return next.getTime();
            }

            scheduleJobInfoService.updateExecuteTime(0L, 0L, id);
            scheduleJobInfoService.updateStatus(ScheduleStatusEnum.CLOSE.getCode(), id);
            return null;
        } catch (ParseException e) {
            scheduleJobInfoService.updateExecuteTime(0L, 0L, id);
            scheduleJobInfoService.updateStatus(ScheduleStatusEnum.CLOSE.getCode(), id);
            return null;
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
    private Date generateNextTime(String cron, Long lastTime) throws ParseException {
        return new CronExpression(cron).getNextValidTimeAfter(new Date(lastTime));
    }
}
