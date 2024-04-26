package com.jimmy.friday.center.core.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.center.base.Close;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.entity.ScheduleJobInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Component
public class ScheduleTimeRing implements Initialize, Close {

    private final ConcurrentMap<Integer, List<ScheduleJobInfo>> ringData = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Autowired
    private ScheduleExecutePool scheduleExecutePool;

    public void push(ScheduleJobInfo scheduleJobInfo) {
        int currentSeconds = (int) ((scheduleJobInfo.getNextTime() / 1000) % 60);

        List<ScheduleJobInfo> ifAbsent = ringData.putIfAbsent(currentSeconds, Lists.newArrayList(scheduleJobInfo));
        if (ifAbsent != null) {
            ifAbsent.add(scheduleJobInfo);
        }
    }

    @Override
    public void init() throws Exception {
        executor.submit((Runnable) () -> {
            while (true) {
                try {
                    //整秒休眠
                    ThreadUtil.sleep(1000 - System.currentTimeMillis() % 1000);

                    List<ScheduleJobInfo> ringItemData = new ArrayList<>();
                    int nowSecond = Calendar.getInstance().get(Calendar.SECOND);   // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                    for (int i = 0; i < 2; i++) {
                        List<ScheduleJobInfo> tmpData = ringData.remove((nowSecond + 60 - i) % 60);
                        if (tmpData != null) {
                            ringItemData.addAll(tmpData);
                        }
                    }

                    if (CollUtil.isNotEmpty(ringItemData)) {
                        for (ScheduleJobInfo scheduleJobInfo : ringItemData) {
                            scheduleExecutePool.execute(scheduleJobInfo);
                        }

                        ringItemData.clear();
                    }
                } catch (Exception e) {
                    log.error("时间轮运行失败", e);
                }
            }
        });
    }

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
