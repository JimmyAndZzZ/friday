package com.jimmy.friday.agent.support;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.jimmy.friday.agent.base.CommandWorker;
import com.jimmy.friday.agent.command.BaseWorker;
import com.jimmy.friday.agent.core.AgentClassLoader;
import com.jimmy.friday.agent.exception.AgentException;
import com.jimmy.friday.boot.enums.agent.CommandTypeEnum;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class CommandSupport {

    private static final String PACKAGE_PATH = "com.jimmy.friday.agent.command";

    private final Map<CommandTypeEnum, CommandWorker> workerMap = new HashMap<>();

    private static CommandSupport commandSupport;

    public static void build() {
        commandSupport = new CommandSupport();
    }

    private CommandSupport() {
        try {
            ClassPath classpath = ClassPath.from(this.getClass().getClassLoader());

            ImmutableSet<ClassPath.ClassInfo> topLevelClasses = classpath.getTopLevelClasses(PACKAGE_PATH);
            for (ClassPath.ClassInfo classInfo : topLevelClasses) {
                String name = classInfo.getName();

                Class<?> clazz = Class.forName(name, true, AgentClassLoader.getDefault());
                //判断是否为抽象类
                if (clazz.equals(BaseWorker.class)) {
                    continue;
                }
                //命令工作类初始化
                if (clazz.getSuperclass().equals(BaseWorker.class) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                    BaseWorker worker = (BaseWorker) clazz.newInstance();
                    workerMap.put(worker.command(), worker);
                }
            }
        } catch (Exception e) {
            throw new AgentException("CommandSupport initialization failed.", e);
        }
    }

    public static CommandSupport get() {
        return commandSupport;
    }

    public CommandWorker getWorker(String cmd) {
        CommandTypeEnum commandTypeEnum = CommandTypeEnum.queryByCmd(cmd);
        return commandTypeEnum == null ? null : workerMap.get(commandTypeEnum);
    }

    public CommandWorker get(CommandTypeEnum commandTypeEnum) {
        return workerMap.get(commandTypeEnum);
    }
}
