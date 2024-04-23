package com.jimmy.friday.boot.core.agent;

import lombok.Data;

import java.util.Date;

@Data
public class Trace {
    private Date date;
    private String logMessage;
    private String className;
    private String methodName;
    private String traceId;
    private String spanId;
    private String level;
    private String param;
    private String result;
    private Boolean isLog;
    private String applicationName;
    private String address;
    private String appId;

    Trace(Date date, String logMessage, String className, String methodName, String traceId, String spanId, String level, String param, String result, Boolean isLog, String applicationName, String address) {
        this.date = date;
        this.logMessage = logMessage;
        this.className = className;
        this.methodName = methodName;
        this.traceId = traceId;
        this.spanId = spanId;
        this.level = level;
        this.param = param;
        this.result = result;
        this.isLog = isLog;
        this.applicationName = applicationName;
        this.address = address;
    }

    public Trace() {

    }

    public static TraceBuilder builder() {
        return new TraceBuilder();
    }

    public String toString() {
        return "Trace(date=" + this.getDate() + ", logMessage=" + this.getLogMessage() + ", className=" + this.getClassName() + ", methodName=" + this.getMethodName() + ", traceId=" + this.getTraceId() + ", spanId=" + this.getSpanId() + ", level=" + this.getLevel() + ", param=" + this.getParam() + ", result=" + this.getResult() + ", isLog=" + this.getIsLog() + ", applicationName=" + this.getApplicationName() + ", address=" + this.getAddress() + ")";
    }

    public static class TraceBuilder {
        private Date date;
        private String logMessage;
        private String className;
        private String methodName;
        private String traceId;
        private String spanId;
        private String level;
        private String param;
        private String result;
        private Boolean isLog;
        private String applicationName;
        private String address;

        TraceBuilder() {
        }

        public TraceBuilder date(Date date) {
            this.date = date;
            return this;
        }

        public TraceBuilder logMessage(String logMessage) {
            this.logMessage = logMessage;
            return this;
        }

        public TraceBuilder className(String className) {
            this.className = className;
            return this;
        }

        public TraceBuilder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public TraceBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public TraceBuilder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public TraceBuilder level(String level) {
            this.level = level;
            return this;
        }

        public TraceBuilder param(String param) {
            this.param = param;
            return this;
        }

        public TraceBuilder result(String result) {
            this.result = result;
            return this;
        }

        public TraceBuilder isLog(Boolean isLog) {
            this.isLog = isLog;
            return this;
        }

        public TraceBuilder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public TraceBuilder address(String address) {
            this.address = address;
            return this;
        }

        public Trace build() {
            return new Trace(this.date, this.logMessage, this.className, this.methodName, this.traceId, this.spanId, this.level, this.param, this.result, this.isLog, this.applicationName, this.address);
        }

        public String toString() {
            return "Trace.TraceBuilder(date=" + this.date + ", logMessage=" + this.logMessage + ", className=" + this.className + ", methodName=" + this.methodName + ", traceId=" + this.traceId + ", spanId=" + this.spanId + ", level=" + this.level + ", param=" + this.param + ", result=" + this.result + ", isLog=" + this.isLog + ", applicationName=" + this.applicationName + ", address=" + this.address + ")";
        }
    }
}
