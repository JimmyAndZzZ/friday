package com.jimmy.friday.agent.base;

public interface Segment {

    boolean write(byte[] bytes);

    byte[] read();

    void free();

    boolean isFree();
}
