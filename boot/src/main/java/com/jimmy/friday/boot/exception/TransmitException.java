package com.jimmy.friday.boot.exception;

public class TransmitException extends RuntimeException {

    public TransmitException(String message) {
        super(message);
    }

    public TransmitException() {
        super();
    }

    public TransmitException(Throwable cause) {
        super(cause);
    }
}
