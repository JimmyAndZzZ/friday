package com.jimmy.friday.boot.exception;

public class ConnectionException extends RuntimeException {

    public ConnectionException() {
        super("服务端不可用");
    }
}
