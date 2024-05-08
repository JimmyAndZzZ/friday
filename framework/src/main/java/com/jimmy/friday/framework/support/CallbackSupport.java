package com.jimmy.friday.framework.support;

import cn.hutool.core.map.MapUtil;
import com.jimmy.friday.boot.base.Callback;
import com.jimmy.friday.boot.enums.gateway.NotifyTypeEnum;
import com.jimmy.friday.boot.message.gateway.InvokeCallback;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallbackSupport {

    private final AtomicBoolean process = new AtomicBoolean(false);

    private final Map<Long, Instant> expiredMap = new ConcurrentHashMap<>();

    private final Map<Long, Callback> callbackMap = new ConcurrentHashMap<>();

    public CallbackSupport() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // 每隔5秒执行一次任务
        scheduler.scheduleAtFixedRate(() -> {
            if (process.compareAndSet(false, true)) {
                try {
                    if (MapUtil.isNotEmpty(expiredMap)) {
                        Instant now = Instant.now();

                        for (Map.Entry<Long, Instant> entry : expiredMap.entrySet()) {
                            Long key = entry.getKey();
                            Instant value = entry.getValue();

                            if (now.isAfter(value)) {
                                Callback remove = callbackMap.remove(key);
                                if (remove != null) {
                                    remove.error("调用超时");
                                }
                            }
                        }
                    }
                } finally {
                    process.set(false);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void registerCallback(Long traceId, Callback callback, Integer timeout) {
        this.callbackMap.put(traceId, callback);
        this.expiredMap.put(traceId, Instant.now().plusSeconds(timeout));
    }

    public void callback(InvokeCallback invokeCallback) {
        Long traceId = invokeCallback.getTraceId();
        Object response = invokeCallback.getResponse();
        String errorMessage = invokeCallback.getErrorMessage();
        Integer progressRate = invokeCallback.getProgressRate();
        NotifyTypeEnum notifyType = invokeCallback.getNotifyType();


        if (notifyType.equals(NotifyTypeEnum.PROGRESS)) {
            Callback callback = callbackMap.get(traceId);
            if (callback != null) {
                callback.progress(progressRate);
            }

            return;
        }

        Callback callback = callbackMap.remove(traceId);
        if (callback == null) {
            return;
        }

        switch (notifyType) {
            case CANCEL:
                callback.error("调用被取消");
                break;
            case TIME_OUT:
                callback.error("调用超时");
                break;
            case COMPLETED:
                callback.finish(response);
                break;
            case ERROR:
                callback.error("调用失败:" + errorMessage);
                break;
        }
    }
}
