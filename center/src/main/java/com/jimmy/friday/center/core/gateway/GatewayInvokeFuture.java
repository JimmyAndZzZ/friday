package com.jimmy.friday.center.core.gateway;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.GatewayInvoke;
import com.jimmy.friday.center.support.TransmitSupport;
import com.jimmy.friday.center.utils.JsonUtil;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GatewayInvokeFuture extends CompletableFuture<GatewayInvoke> {

    private static final Map<Long, String> SERVICE_ID_MAP = Maps.newConcurrentMap();

    private static final Map<Long, CompletableFuture<GatewayInvoke>> FUTURES = Maps.newConcurrentMap();

    private final Long traceId;

    private final long timeout;

    private final Channel channel;

    private final GatewayInvoke gatewayInvoke;

    private final TransmitSupport transmitSupport;

    public GatewayInvokeFuture(GatewayInvoke gatewayInvoke, Channel channel, long timeout, TransmitSupport transmitSupport) {
        this.timeout = timeout;
        this.channel = channel;
        this.gatewayInvoke = gatewayInvoke;
        this.transmitSupport = transmitSupport;
        this.traceId = gatewayInvoke.getTraceId();
        FUTURES.put(traceId, this);
        SERVICE_ID_MAP.put(traceId, gatewayInvoke.getApplicationId());
    }

    public static CompletableFuture<GatewayInvoke> getAndClear(Long id) {
        return FUTURES.remove(id);
    }

    public static Map<Long, String> getServiceIdAndTraceId() {
        return SERVICE_ID_MAP;
    }

    @Override
    public GatewayInvoke get() {
        return this.get(timeout, TimeUnit.SECONDS);
    }

    @Override
    public GatewayInvoke get(long timeout, TimeUnit unit) {
        try {
            transmitSupport.transmit(this.gatewayInvoke,channel);
            return super.get(timeout, unit);
        } catch (InterruptedException e) {
            gatewayInvoke.setIsSuccess(false);
            gatewayInvoke.setError("调用被中断");
            gatewayInvoke.setExceptionClass(InterruptedException.class.getName());
            return gatewayInvoke;
        } catch (ExecutionException e) {
            gatewayInvoke.setIsSuccess(false);
            gatewayInvoke.setError(e.getMessage());
            gatewayInvoke.setExceptionClass(ExecutionException.class.getName());
            return gatewayInvoke;
        } catch (TimeoutException e) {
            gatewayInvoke.setIsSuccess(false);
            gatewayInvoke.setError("调用超时");
            gatewayInvoke.setIsNeedRetry(true);
            gatewayInvoke.setExceptionClass(TimeoutException.class.getName());
            return gatewayInvoke;
        } finally {
            FUTURES.remove(traceId);
            SERVICE_ID_MAP.remove(traceId);
        }
    }
}
