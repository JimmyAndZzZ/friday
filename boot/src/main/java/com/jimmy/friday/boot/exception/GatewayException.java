package com.jimmy.friday.boot.exception;

import lombok.Getter;

/**
 * 中断异常
 */
@Getter
public class GatewayException extends RuntimeException {

    private int code = 500;

    private String message;

    public GatewayException(String message) {
        super(message);
        this.message = message;
    }

    public GatewayException(int code, String desc, Exception e) {
        super(e);
        this.code = code;
        this.message = desc;
    }

    public GatewayException(int code, String desc) {
        super(desc);
        this.code = code;
        this.message = desc;
    }

    public GatewayException() {
        super();
    }

    public GatewayException(Throwable cause) {
        super(cause);
    }
}
