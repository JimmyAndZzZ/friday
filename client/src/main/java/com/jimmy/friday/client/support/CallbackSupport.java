package com.jimmy.friday.client.support;

import cn.hutool.core.map.MapUtil;
import com.jimmy.friday.boot.base.Callback;
import com.jimmy.friday.boot.enums.NotifyTypeEnum;
import com.jimmy.friday.boot.message.gateway.InvokeCallback;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallbackSupport {

    private static final AtomicBoolean PROCESS = new AtomicBoolean(false);

    private static final Map<Long, Instant> EXPIRED_MAP = new ConcurrentHashMap<>();

    private static final Map<Long, Callback> CALLBACK_MAP = new ConcurrentHashMap<>();

    static {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // 每隔5秒执行一次任务
        scheduler.scheduleAtFixedRate(() -> {
            if (PROCESS.compareAndSet(false, true)) {
                try {
                    if (MapUtil.isNotEmpty(EXPIRED_MAP)) {
                        Instant now = Instant.now();

                        for (Map.Entry<Long, Instant> entry : EXPIRED_MAP.entrySet()) {
                            Long key = entry.getKey();
                            Instant value = entry.getValue();

                            if (now.isAfter(value)) {
                                Callback remove = CALLBACK_MAP.remove(key);
                                if (remove != null) {
                                    remove.error("调用超时");
                                }
                            }
                        }
                    }
                } finally {
                    PROCESS.set(false);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static void registerCallback(Long traceId, Callback callback, Integer timeout) {
        CALLBACK_MAP.put(traceId, callback);
        EXPIRED_MAP.put(traceId, Instant.now().plusSeconds(timeout));
    }

    public static void callback(InvokeCallback invokeCallback) {
        Long traceId = invokeCallback.getTraceId();
        Object response = invokeCallback.getResponse();
        String errorMessage = invokeCallback.getErrorMessage();
        Integer progressRate = invokeCallback.getProgressRate();
        NotifyTypeEnum notifyType = invokeCallback.getNotifyType();

        if (notifyType.equals(NotifyTypeEnum.PROGRESS)) {
            Callback callback = CALLBACK_MAP.get(traceId);
            if (callback != null) {
                callback.progress(progressRate);
            }

            return;
        }

        Callback callback = CALLBACK_MAP.remove(traceId);
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
