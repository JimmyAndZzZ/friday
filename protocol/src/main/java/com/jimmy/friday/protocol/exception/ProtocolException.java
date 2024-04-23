package com.jimmy.friday.protocol.exception;

public class ProtocolException extends RuntimeException {

    /**
     * 错误异常Code SystemErrorCodeEnum code 一一对应
     */
    protected int code;
    /**
     * 自己的日常 打log 用
     */
    protected String message;

    protected Boolean isPrint = false;

    public ProtocolException(String message) {
        super(message);
        this.message = message;
    }

    public ProtocolException(String message, Boolean isPrint) {
        super(message);
        this.message = message;
        this.isPrint = isPrint;
    }

    public ProtocolException(int code, String desc, Exception e) {
        super(e);
        this.code = code;
        this.message = desc;
    }

    public ProtocolException() {
        super();
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    public Boolean getPrint() {
        return isPrint;
    }
}
