package com.jimmy.friday.protocol.base;

public interface Push extends Output {

    Object send(String message, String userId);
}
