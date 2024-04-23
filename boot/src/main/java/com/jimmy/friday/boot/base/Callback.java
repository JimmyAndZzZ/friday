package com.jimmy.friday.boot.base;

public interface Callback {

    void progress(Integer progressRate);

    void finish(Object result);

    void error(String errorMessage);
}
