package com.jimmy.friday.boot.core.agent;

import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.boot.result.TraceResult;
import com.jimmy.friday.boot.result.WatchResult;

public class ContextHold {

    private static final ThreadLocal<Context> HOLD = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> QPS_FLAG_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<String> SQL_ID = new ThreadLocal<>();

    private static final ThreadLocal<WatchResult> WATCH_RESULT_THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<TraceResult> TRACE_RESULT_THREAD_LOCAL = new ThreadLocal<>();

    public static Context getContext() {
        return HOLD.get();
    }

    public static String getSqlId() {
        String s = SQL_ID.get();
        if (s != null && !s.isEmpty()) {
            return s;
        }

        s = ShortUUID.uuid();
        SQL_ID.set(s);
        return s;
    }

    public static String getAndRemoveSqlId() {
        String s = SQL_ID.get();
        SQL_ID.remove();
        return s;
    }

    public static void setContext(Context context) {
        HOLD.set(context);
    }

    public static void removeContext() {
        HOLD.remove();
    }

    public static Boolean getQpsFlagHolder() {
        Boolean aBoolean = QPS_FLAG_HOLDER.get();
        return aBoolean != null ? aBoolean : false;
    }

    public static void setQpsFlagHolder(Boolean flag) {
        QPS_FLAG_HOLDER.set(flag);
    }

    public static void removeQpsFlagHolder() {
        QPS_FLAG_HOLDER.remove();
    }

    public static TraceResult getTraceResult() {
        return TRACE_RESULT_THREAD_LOCAL.get();
    }

    public static void setTraceResult(TraceResult c) {
        TRACE_RESULT_THREAD_LOCAL.set(c);
    }

    public static void removeTraceResult() {
        TRACE_RESULT_THREAD_LOCAL.remove();
    }

    public static WatchResult getWatchResult() {
        return WATCH_RESULT_THREAD_LOCAL.get();
    }

    public static void setWatchResult(WatchResult c) {
        WATCH_RESULT_THREAD_LOCAL.set(c);
    }

    public static void removeWatchResult() {
        WATCH_RESULT_THREAD_LOCAL.remove();
    }
}
