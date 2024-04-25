package com.jimmy.friday.boot.exception;

import lombok.Getter;

/**
 * 中断异常
 */
public class TransactionException extends RuntimeException {

    /**
     * 错误异常Code SystemErrorCodeEnum code 一一对应
     */
    @Getter
    private int code = 500;
    /**
     * 自己的日常 打log 用
     */
    private String message;

    public TransactionException(String message) {
        super(message);
        this.message = message;
    }


    public TransactionException(int code, String desc, Exception e) {
        super(e);
        this.code = code;
        this.message = desc;
    }

    public TransactionException(int code, String desc) {
        super(desc);
        this.code = code;
        this.message = desc;
    }

    public TransactionException() {
        super();
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }
}
