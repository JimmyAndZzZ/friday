package com.jimmy.friday.agent.core;

import com.google.common.collect.Sets;

import java.util.Set;

public class AnnotationLoad {

    private static final Set<String> QPS_ADVICE = Sets.newHashSet();

    private static final Set<String> TRACE_ADVICE = Sets.newHashSet();

    public static void putTracePoint(String className, String methodName) {
        TRACE_ADVICE.add(className + "#" + methodName);
    }

    public static boolean traceIsMatch(String className, String methodName) {
        return TRACE_ADVICE.contains(className + "#" + methodName);
    }

    public static void putQpsPoint(String className, String methodName) {
        QPS_ADVICE.add(className + "#" + methodName);
    }

    public static boolean qpsIsMatch(String className, String methodName) {
        return QPS_ADVICE.contains(className + "#" + methodName);
    }
}
