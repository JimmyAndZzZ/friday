package com.jimmy.friday.center.core;

import com.google.common.collect.Lists;
import com.jimmy.friday.center.base.Hook;

import java.util.List;

public class GatewaySession {

    private final static ThreadLocal<GatewaySession> HOLDER = ThreadLocal.withInitial(GatewaySession::new);

    private Long traceId;

    private String serviceId;

    private Boolean isFallback = false;

    private final List<Hook> hooks = Lists.newArrayList();

    public static Boolean getFallback() {
        return HOLDER.get().isFallback;
    }

    public static void setFallback(Boolean fallback) {
        HOLDER.get().isFallback = fallback;
    }

    private Boolean isRoute = false;

    public static List<Hook> getHooks() {
        return HOLDER.get().hooks;
    }

    public static void addHook(Hook hook) {
        HOLDER.get().hooks.add(hook);
    }

    public static String getServiceId() {
        return HOLDER.get().serviceId;
    }

    public static void setServiceId(String serviceId) {
        HOLDER.get().serviceId = serviceId;
    }

    public static Long getTraceId() {
        return HOLDER.get().traceId;
    }

    public static void setTraceId(Long traceId) {
        HOLDER.get().traceId = traceId;
    }

    public static Boolean getIsRoute() {
        return HOLDER.get().isRoute;
    }

    public static void setIsRoute(Boolean route) {
        HOLDER.get().isRoute = route;
    }

    public static void clear() {
        HOLDER.remove();
    }


}
