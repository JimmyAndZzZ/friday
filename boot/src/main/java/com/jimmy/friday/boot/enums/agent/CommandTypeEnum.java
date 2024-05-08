package com.jimmy.friday.boot.enums.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandTypeEnum {

    TRACE("trace"),
    WATCH("watch"),
    JVM("jvm"),
    CD("cd"),
    LS("ls"),
    SQL("sql");

    private String cmd;

    public static CommandTypeEnum queryByCmd(String cmd) {
        for (CommandTypeEnum value : CommandTypeEnum.values()) {
            if (value.cmd.equalsIgnoreCase(cmd)) {
                return value;
            }
        }
        return null;
    }
}
