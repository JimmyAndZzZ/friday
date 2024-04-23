package com.jimmy.friday.agent.other.jdbc;

public class URLLocation {
    private final int startIndex;
    private final int endIndex;

    public URLLocation(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int startIndex() {
        return startIndex;
    }

    public int endIndex() {
        return endIndex;
    }
}
