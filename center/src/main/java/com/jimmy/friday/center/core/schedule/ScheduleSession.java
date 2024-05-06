package com.jimmy.friday.center.core.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.schedule.ScheduleExecutor;
import com.jimmy.friday.boot.core.schedule.ScheduleRunInfo;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.service.ScheduleExecutorService;
import com.jimmy.friday.center.utils.LockKeyConstants;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class ScheduleSession implements Initialize {

    private static final int MAX_HEARTBEAT_FAIL_COUNT = 3;

    private final Map<String, ScheduleExecutor> executor = Maps.newHashMap();

    private final Map<String, List<ScheduleRunInfo>> runInfo = Maps.newHashMap();

    private final ConcurrentMap<String, Set<String>> session = Maps.newConcurrentMap();

    private final ConcurrentMap<String, AtomicInteger> heartbeatFailCheck = Maps.newConcurrentMap();

    private final Cache<String, Long> heartbeatSign = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS) // 设置过期时间为1分钟
            .build();

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        // 定时任务，每隔三分钟执行一次
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            if (MapUtil.isNotEmpty(session)) {
                for (Map.Entry<String, Set<String>> entry : session.entrySet()) {
                    String key = entry.getKey();
                    Set<String> value = entry.getValue();

                    if (CollUtil.isNotEmpty(value)) {
                        Iterator<String> iterator = value.iterator();

                        while (iterator.hasNext()) {
                            String applicationId = iterator.next();

                            Long ifPresent = this.heartbeatSign.getIfPresent(applicationId);
                            if (ifPresent == null) {
                                log.error("应用{},id:{}，三分钟内没有心跳", key, applicationId);

                                AtomicInteger atomicInteger = heartbeatFailCheck.computeIfAbsent(applicationId, s -> new AtomicInteger(0));
                                int i = atomicInteger.incrementAndGet();
                                //心跳失联次数过大
                                if (i >= MAX_HEARTBEAT_FAIL_COUNT) {
                                    log.error("应用{},id:{}，被剔除", key, applicationId);
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            }
        }, 0, 3, TimeUnit.MINUTES);
    }

    @Override
    public int sort() {
        return 1;
    }

    public void heartbeat(String applicationId, String applicationName, List<ScheduleRunInfo> scheduleRunInfoList) {
        Set<String> put = this.session.computeIfAbsent(applicationName, k -> Sets.newHashSet());
        put.add(applicationId);

        this.heartbeatFailCheck.remove(applicationId);
        this.heartbeatSign.put(applicationId, System.currentTimeMillis());
        this.runInfo.put(applicationId, scheduleRunInfoList);
    }

    public void connect(String applicationId, String applicationName, String ip) {
        Lock lock = this.stripedLock.getLocalLock(LockKeyConstants.Schedule.SCHEDULE_EXECUTOR_SESSION, 8, applicationId);
        lock.lock();

        try {
            Set<String> put = this.session.computeIfAbsent(applicationName, k -> Sets.newHashSet());
            put.add(applicationId);

            ScheduleExecutor exist = this.executor.get(applicationId);
            if (exist != null) {
                //下线原有
                this.scheduleExecutorService.offline(exist.getApplicationName(), exist.getIp());

                exist.setIp(ip);
                exist.setApplicationId(applicationId);
                exist.setApplicationName(applicationName);
            } else {
                ScheduleExecutor executor = new ScheduleExecutor();
                executor.setIp(ip);
                executor.setApplicationId(applicationId);
                executor.setApplicationName(applicationName);
                this.executor.put(applicationId, executor);
            }

            this.scheduleExecutorService.register(applicationName, ip);
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

        List<ScheduleExecutor> executors = Lists.newArrayList();

        for (String applicationId : applicationIds) {
            if (CollUtil.isNotEmpty(ignore) && ignore.contains(applicationId)) {
                continue;
            }
            //心跳异常
            Long ifPresent = this.heartbeatSign.getIfPresent(applicationId);
            if (ifPresent == null) {
                continue;
            }

            ScheduleExecutor executor = this.executor.get(applicationId);
            if (executor == null) {
                continue;
            }

            executor.setWeight(this.getWeight(applicationId));
            executor.setLastInvokeTime(this.getLastInvokeTime(applicationId));
        }

        if (CollUtil.isEmpty(executors)) {
            return null;
        }

        ScheduleExecutor route = this.route(executors);
        route.setId(this.scheduleExecutorService.query(route.getApplicationName(), route.getIp()).getId());
        return route;
    }

    /**
     * 路由选择
     *
     * @param executors
     * @return
     */
    private ScheduleExecutor route(List<ScheduleExecutor> executors) {
        if (executors.size() == 1) {
            return executors.stream().findFirst().get();
        }
        // 计算总权重
        int totalWeight = executors.stream().mapToInt(ScheduleExecutor::getWeight).sum();
        // 计算每个执行器被选取的综合概率
        List<Double> probabilities = new ArrayList<>();
        for (ScheduleExecutor executor : executors) {
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
}
