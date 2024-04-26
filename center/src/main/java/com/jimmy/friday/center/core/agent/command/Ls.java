package com.jimmy.friday.center.core.agent.command;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.enums.CommandTypeEnum;
import com.jimmy.friday.center.base.Handler;
import com.jimmy.friday.center.core.agent.support.AgentSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class Ls implements Handler {

    @Autowired
    private AgentSupport agentSupport;

    @Override
    public String execute(Command command, String sessionKey) {
        Set<String> appList = agentSupport.getAppList();
        return CollUtil.isEmpty(appList) ? StrUtil.EMPTY : CollUtil.join(appList, StrUtil.SPACE + StrUtil.SPACE);
    }

    @Override
    public CommandTypeEnum type() {
        return CommandTypeEnum.LS;
    }

    @Override
    public boolean isCover() {
        return false;
    }
}
