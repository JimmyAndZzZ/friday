package com.jimmy.friday.agent.other;

import com.jimmy.friday.agent.support.CommandSupport;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.RunLine;
import com.jimmy.friday.boot.enums.agent.CommandTypeEnum;
import com.jimmy.friday.boot.result.TraceResult;
import com.jimmy.friday.boot.result.WatchResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.BitSet;
import java.util.List;

public class RunMonitorPool {

    private static final MultiMap<String, Long> TRACE_WAIT = new MultiMap<>();

    private static final MultiMap<String, Long> WATCH_WAIT = new MultiMap<>();

    public static void process(RunLine traceLine) {
        CommandSupport.get().get(CommandTypeEnum.TRACE).process(traceLine);
        CommandSupport.get().get(CommandTypeEnum.WATCH).process(traceLine);
    }

    public static void finish(CommandTypeEnum commandTypeEnum) {
        CommandSupport.get().get(commandTypeEnum).finish();
    }

    public static void monitor(Command command) {
        String cmd = command.getCommand();
        Long traceId = command.getTraceId();
        List<String> param = command.getParam();

        if (param == null || param.isEmpty()) {
            throw new IllegalArgumentException("参数为空");
        }

        if (param.size() < 2) {
            throw new IllegalArgumentException("参数个数异常");
        }

        String clazz = param.get(0);
        String method = param.get(1);

        switch (cmd.toUpperCase()) {
            case "TRACE":
                TRACE_WAIT.put(clazz + "." + method, traceId);
                break;
            case "WATCH":
                WATCH_WAIT.put(clazz + "." + method, traceId);
                break;
        }
    }

    public static BitSet match(String clazz, String method) {
        boolean isTrace = false;
        boolean isWatch = false;
        BitSet results = new BitSet(2);
        String key = clazz + "." + method;

        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = localDateTime.format(formatter);

        TraceResult traceResult = ContextHold.getTraceResult();
        if (traceResult == null) {
            Long traceId = TRACE_WAIT.get(key);
            if (traceId != null) {
                traceResult = new TraceResult();

                ContextHold.setTraceResult(traceResult);

                traceResult.setTraceId(traceId);
                traceResult.setTs(now);
                traceResult.setThreadName(Thread.currentThread().getName());

                isTrace = true;

                TRACE_WAIT.remove(key);
            }
        }
        WatchResult watchResult = ContextHold.getWatchResult();
        if (watchResult == null) {
            Long watchId = WATCH_WAIT.get(key);
            if (watchId != null) {
                watchResult = new WatchResult();

                ContextHold.setWatchResult(watchResult);

                watchResult.setTraceId(watchId);
                watchResult.setTs(now);

                isWatch = true;

                WATCH_WAIT.remove(key);
            }
        }

        results.set(0, isTrace);
        results.set(1, isWatch);
        return results;
    }
}
