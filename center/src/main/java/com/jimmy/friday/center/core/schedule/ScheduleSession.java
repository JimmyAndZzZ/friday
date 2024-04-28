package com.jimmy.friday.center.core.schedule;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.schedule.ScheduleRunInfo;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.service.ScheduleExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ScheduleSession {

    private final ConcurrentMap<String, Set<String>> session = Maps.newConcurrentMap();

    private final ConcurrentMap<String, List<ScheduleRunInfo>> runInfo = Maps.newConcurrentMap();

    private final Cache<String, Long> heartbeatSign = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS) // 设置过期时间为1分钟
            .build();

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    public void heartbeat(String applicationId, String applicationName, String ip, List<ScheduleRunInfo> scheduleRunInfoList) {
        this.heartbeatSign.put(applicationId, System.currentTimeMillis());
        this.connect(applicationId, applicationName, ip);
        this.runInfo.put(applicationId, scheduleRunInfoList);
    }


    public void connect(String applicationId, String applicationName, String ip) {
        Set<String> put = session.computeIfAbsent(applicationName, k -> Sets.newHashSet());
        put.add(applicationId);
        scheduleExecutorService.register(applicationName, ip);
    }

    public void disconnect(String applicationId, String applicationName, String ip) {
        session.computeIfPresent(applicationName, (key, value) -> {
            value.remove(applicationId);
            return value.isEmpty() ? null : value;
        });

        scheduleExecutorService.offline(applicationName, ip);
    }

    public List<ScheduleRunInfo> getRealTimeRunInfo(String applicationId) {
        return this.runInfo.get(applicationId);
    }

    public String getApplicationId(String applicationName) {
        return null;

    }

}
