package com.jimmy.friday.boot.other;

public interface GlobalConstants {

    int DEFAULT_TIMEOUT = 30;

    String DEFAULT_VERSION = "release-1.0.0";

    interface Center {
        String ROOT = "/";

        int TIMESTAMP_MAX_DIFFERENCE = 1000 * 60 * 5;
    }

    interface Client {

        String DEFAULT_OFFSET_PATH = "/data/gateway/offset/";

        int PUSH_MESSAGE_WAIT_TIME = 30;
    }

}
