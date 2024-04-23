package com.jimmy.friday.center.other;


import com.jimmy.friday.boot.core.agent.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    public static Command parseCommand(String commandString, Long traceId) {
        List<String> commandAndArgs = new ArrayList<>();
        String[] tokens = commandString.trim().split("\\s+");

        if (tokens.length == 0) {
            return null;
        }

        for (int i = 1; i < tokens.length; i++) {
            if (!tokens[i].isEmpty()) {
                commandAndArgs.add(tokens[i]); // 添加参数
            }
        }

        Command command = new Command();
        command.setTraceId(traceId);
        command.setCommand(tokens[0]);
        command.setParam(commandAndArgs);
        return command;
    }
}
