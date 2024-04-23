package com.jimmy.friday.agent.utils;

public class DockerUtil {

    public static String getTaskSlot() {
        return System.getenv("MY_TASK_SLOT");
    }
}
