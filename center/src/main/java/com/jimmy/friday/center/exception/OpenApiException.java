package com.jimmy.friday.center.exception;

import lombok.Getter;

@Getter
public class OpenApiException extends RuntimeException {

    /**
     * 错误异常Code SystemErrorCodeEnum code 一一对应
     */

    private int code = 500;
    /**
     * 自己的日常 打log 用
     */
    private String message;

    public OpenApiException(String message) {
        super(message);
        this.message = message;
    }


    public OpenApiException(int code, String desc, Exception e) {
        super(e);
        this.code = code;
        this.message = desc;
    }

    public OpenApiException(int code, String desc) {
        super(desc);
        this.code = code;
        this.message = desc;
    }

    public OpenApiException() {
        super();
    }

    public OpenApiException(Throwable cause) {
        super(cause);
    }
}

