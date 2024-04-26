package com.jimmy.friday.center.core.schedule;

import com.jimmy.friday.center.base.Close;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.entity.ScheduleJobInfo;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ScheduleExecutePool implements Close {

    private final ThreadPoolExecutor highPool = new ThreadPoolExecutor(10, 200, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), r -> new Thread(r, "friday-schedule-high-" + r.hashCode()));

    private final ThreadPoolExecutor normalPool = new ThreadPoolExecutor(7, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1500), r -> new Thread(r, "friday-schedule-normal-" + r.hashCode()));

    private final ThreadPoolExecutor lowPool = new ThreadPoolExecutor(5, 50, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2000), r -> new Thread(r, "friday-schedule-low-" + r.hashCode()));

    @Autowired
    private AttachmentCache attachmentCache;


    public void execute(ScheduleJobInfo scheduleJobInfo) {
        Integer id = scheduleJobInfo.getId();
        //默认用高
        ThreadPoolExecutor executor = this.highPool;

        Long executeCount = attachmentCache.increment(RedisConstants.Schedule.SCHEDULE_EXECUTE_COUNT + id);
        attachmentCache.expire(RedisConstants.Schedule.SCHEDULE_EXECUTE_COUNT + id, 60L, TimeUnit.SECONDS);
        //一分钟内执行10次以上则降级
        if (executeCount >= 10) {
            executor = this.normalPool;
        }

        Long executeFailCount = attachmentCache.increment(RedisConstants.Schedule.SCHEDULE_EXECUTE_FAIL_COUNT + id);
        //五分钟内失败10次以上
        if (executeFailCount >= 10) {
            executor = this.lowPool;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void close() {
        this.highPool.shutdown();
        this.normalPool.shutdown();
        this.lowPool.shutdown();
    }
}
