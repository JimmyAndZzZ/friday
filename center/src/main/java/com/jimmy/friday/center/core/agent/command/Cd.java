package com.jimmy.friday.center.core.agent.command;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.enums.agent.CommandTypeEnum;
import com.jimmy.friday.center.base.agent.Handler;
import com.jimmy.friday.center.core.agent.CommandSession;
import com.jimmy.friday.center.core.agent.support.AgentSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Cd implements Handler {

    @Autowired
    private CommandSession session;

    @Autowired
    private AgentSupport agentSupport;

    @Override
    public String execute(Command command, String sessionKey) {
        List<String> param = command.getParam();
        if (CollUtil.isEmpty(param)) {
            return StrUtil.EMPTY;
        }

        String s = param.stream().findFirst().get();

        if (!agentSupport.getAppList().contains(s)) {
            return s + ":该应用不存在";
        }

        session.cd(sessionKey, s);
        return "[" + s + "]#" + StrUtil.SPACE;
    }

    @Override
    public CommandTypeEnum type() {
        return CommandTypeEnum.CD;
    }

    @Override
    public boolean isCover() {
        return true;
    }
}
