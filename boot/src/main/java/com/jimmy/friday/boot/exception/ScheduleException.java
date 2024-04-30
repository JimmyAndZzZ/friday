package com.jimmy.friday.boot.exception;

/**
 * 中断异常
 */
public class ScheduleException extends RuntimeException {

    public ScheduleException(String message) {
        super(message);
    }

    public ScheduleException() {
        super();
    }

    public ScheduleException(Throwable cause) {
        super(cause);
    }
}
