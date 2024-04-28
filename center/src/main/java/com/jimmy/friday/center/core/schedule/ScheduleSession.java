package com.jimmy.friday.center.core.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.schedule.ScheduleRunInfo;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.ScheduleExecutor;
import com.jimmy.friday.center.service.ScheduleExecutorService;
import com.jimmy.friday.center.utils.LockKeyConstants;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class ScheduleSession {

    private final Map<String, Executor> executor = Maps.newHashMap();

    private final Map<String, List<ScheduleRunInfo>> runInfo = Maps.newHashMap();

    private final ConcurrentMap<String, Set<String>> session = Maps.newConcurrentMap();

    private final Cache<String, Long> heartbeatSign = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS) // 设置过期时间为1分钟
            .build();

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    public void heartbeat(String applicationId, List<ScheduleRunInfo> scheduleRunInfoList) {
        this.heartbeatSign.put(applicationId, System.currentTimeMillis());
        this.runInfo.put(applicationId, scheduleRunInfoList);
    }

    public void connect(String applicationId, String applicationName, String ip) {
        Lock lock = this.stripedLock.getLocalLock(LockKeyConstants.Schedule.SCHEDULE_EXECUTOR_SESSION, 8, applicationId);
        lock.lock();

        try {
            Set<String> put = this.session.computeIfAbsent(applicationName, k -> Sets.newHashSet());
            put.add(applicationId);

            Executor executor = new Executor();
            executor.setIp(ip);
            executor.setApplicationId(applicationId);
            executor.setApplicationName(applicationName);
            this.executor.put(applicationId, executor);

            this.scheduleExecutorService.register(applicationName, ip);
        } finally {
            lock.unlock();
        }
    }

    public void disconnect(String applicationId, String applicationName, String ip) {
        Lock lock = stripedLock.getLocalLock(LockKeyConstants.Schedule.SCHEDULE_EXECUTOR_SESSION, 8, applicationId);
        lock.lock();

        try {
            session.computeIfPresent(applicationName, (key, value) -> {
                value.remove(applicationId);
                return value.isEmpty() ? null : value;
            });

            this.executor.remove(applicationId);
            this.scheduleExecutorService.offline(applicationName, ip);
        } finally {
            lock.unlock();
        }
    }

    public List<ScheduleRunInfo> getRealTimeRunInfo(String applicationId) {
        return this.runInfo.get(applicationId);
    }

    public void setWeight(String applicationId, Integer weight) {
        attachmentCache.attachString(RedisConstants.Schedule.SCHEDULE_EXECUTOR_APPLICATION_WEIGHT, applicationId, weight.toString());
    }

    public Integer getWeight(String applicationId) {
        return Convert.toInt(attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_EXECUTOR_APPLICATION_WEIGHT, applicationId), 0);
    }

    public void setLastInvokeTime(String applicationId, Long lastInvokeTime) {
        attachmentCache.attachString(RedisConstants.Schedule.SCHEDULE_EXECUTOR_LAST_INVOKE_DATE, applicationId, lastInvokeTime.toString());
    }

    public Long getLastInvokeTime(String applicationId) {
        return Convert.toLong(attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_EXECUTOR_LAST_INVOKE_DATE, applicationId));
    }

    public ScheduleExecutor select(String applicationName, Set<String> ignore) {
        Set<String> applicationIds = this.session.get(applicationName);
        if (CollUtil.isEmpty(applicationIds)) {
            return null;
        }

        List<Executor> executors = Lists.newArrayList();

        for (String applicationId : applicationIds) {
            if (CollUtil.isNotEmpty(ignore) && ignore.contains(applicationId)) {
                continue;
            }
            //心跳异常
            Long ifPresent = this.heartbeatSign.getIfPresent(applicationId);
            if (ifPresent == null) {
                continue;
            }

            Executor executor = this.executor.get(applicationId);
            if (executor == null) {
                continue;
            }

            executor.setWeight(this.getWeight(applicationId));
            executor.setLastInvokeTime(this.getLastInvokeTime(applicationId));
        }

        if (CollUtil.isEmpty(executors)) {
            return null;
        }

        Executor route = this.route(executors);
        return this.scheduleExecutorService.query(route.getApplicationName(), route.getIp());
    }


    private String getApplicationId(ScheduleExecutor scheduleExecutor) {
        String ipAddress = scheduleExecutor.getIpAddress();
        String applicationName = scheduleExecutor.getApplicationName();


    }

    /**
     * 路由选择
     *
     * @param executors
     * @return
     */
    private Executor route(List<Executor> executors) {
        if (executors.size() == 1) {
            return executors.stream().findFirst().get();
        }
        // 计算总权重
        int totalWeight = executors.stream().mapToInt(Executor::getWeight).sum();
        // 计算每个执行器被选取的综合概率
        List<Double> probabilities = new ArrayList<>();
        for (Executor executor : executors) {
            double weightProb = 0.8 * ((double) executor.getWeight() / totalWeight);
            double timeProb = 0.2 * ((double) (System.currentTimeMillis() - executor.getLastInvokeTime()) / System.currentTimeMillis());
            double totalProb = weightProb + timeProb;
            probabilities.add(totalProb);
        }
        // 随机选择一个执行器
        double rand = new Random().nextDouble();
        double cumulativeProb = 0;
        for (int i = 0; i < executors.size(); i++) {
            cumulativeProb += probabilities.get(i);
            if (rand <= cumulativeProb) {
                return executors.get(i);
            }
        }

        return executors.get(executors.size() - 1);
    }

    @Data
    private static class Executor implements Serializable {

        private String applicationName;

        private String ip;

        private Long lastInvokeTime;

        private Integer weight;

        private String applicationId;
    }
}
