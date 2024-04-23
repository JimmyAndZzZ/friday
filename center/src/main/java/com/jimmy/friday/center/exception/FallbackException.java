package com.jimmy.friday.center.exception;

import lombok.Getter;

@Getter
public class FallbackException extends RuntimeException {

    private Throwable throwable;

    public FallbackException(Throwable throwable) {
        super(throwable);
        this.throwable = throwable;
    }
}