package com.jimmy.friday.boot.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private int code = 500;

    private String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(int code, String desc, Exception e) {
        super(e);
        this.code = code;
        this.message = desc;
    }

    public BusinessException(int code, String desc) {
        super(desc);
        this.code = code;
        this.message = desc;
    }

    public BusinessException() {
        super();
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }
}
