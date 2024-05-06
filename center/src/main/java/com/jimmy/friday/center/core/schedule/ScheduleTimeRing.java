package com.jimmy.friday.center.core.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.entity.ScheduleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ScheduleTimeRing implements Initialize {

    private final ConcurrentMap<Integer, List<ScheduleJob>> ringData = new ConcurrentHashMap<>();

    @Autowired
    private ScheduleExecutePool scheduleExecutePool;

    public void push(ScheduleJob scheduleJob) {
        int currentSeconds = (int) ((scheduleJob.getNextTime() / 1000) % 60);

        List<ScheduleJob> ifAbsent = ringData.putIfAbsent(currentSeconds, Lists.newArrayList(scheduleJob));
        if (ifAbsent != null) {
            ifAbsent.add(scheduleJob);
        }
    }

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    //整秒休眠
                    ThreadUtil.sleep(1000 - System.currentTimeMillis() % 1000);

                    List<ScheduleJob> ringItemData = new ArrayList<>();
                    int nowSecond = Calendar.getInstance().get(Calendar.SECOND);   // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                    for (int i = 0; i < 2; i++) {
                        List<ScheduleJob> tmpData = ringData.remove((nowSecond + 60 - i) % 60);
                        if (tmpData != null) {
                            ringItemData.addAll(tmpData);
                        }
                    }

                    if (CollUtil.isNotEmpty(ringItemData)) {
                        for (ScheduleJob scheduleJobInfo : ringItemData) {
                            scheduleExecutePool.execute(scheduleJobInfo);
                        }

                        ringItemData.clear();
                    }
                } catch (Exception e) {
                    log.error("时间轮运行失败", e);
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public int sort() {
        return 0;
    }
}
