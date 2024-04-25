package com.jimmy.friday.boot.exception;

import lombok.Getter;

/**
 * 中断异常
 */
public class ScheduleException extends RuntimeException {

    /**
     * 错误异常Code SystemErrorCodeEnum code 一一对应
     */
    @Getter
    private int code = 500;
    /**
     * 自己的日常 打log 用
     */
    private String message;

    public ScheduleException(String message) {
        super(message);
        this.message = message;
    }


    public ScheduleException(int code, String desc, Exception e) {
        super(e);
        this.code = code;
        this.message = desc;
    }

    public ScheduleException(int code, String desc) {
        super(desc);
        this.code = code;
        this.message = desc;
    }

    public ScheduleException() {
        super();
    }

    public ScheduleException(Throwable cause) {
        super(cause);
    }
}
