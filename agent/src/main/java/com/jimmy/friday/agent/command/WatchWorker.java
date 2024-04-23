package com.jimmy.friday.agent.command;

import com.google.common.base.Strings;
import com.jimmy.friday.agent.other.RunMonitorPool;
import com.jimmy.friday.agent.support.TransmitSupport;
import com.jimmy.friday.agent.utils.JsonUtil;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.RunLine;
import com.jimmy.friday.boot.enums.CommandTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentCommand;
import com.jimmy.friday.boot.result.WatchResult;

public class WatchWorker extends BaseWorker<RunLine> {

    @Override
    public void open(Command command) {
        RunMonitorPool.monitor(command);
    }

    @Override
    public void process(RunLine traceLine) {
        WatchResult watchResult = ContextHold.getWatchResult();
        if (watchResult != null) {
            watchResult.setRunLine(traceLine);
        }
    }

    @Override
    public void finish() {
        WatchResult watchResult = ContextHold.getWatchResult();
        if (watchResult != null) {
            ContextHold.removeWatchResult();

            RunLine runLine = watchResult.getRunLine();
            if (runLine != null) {
                String param = runLine.getParam();
                Object returnValue = runLine.getReturnValue();

                StringBuilder sb = new StringBuilder();
                sb.append("ts=").append(watchResult.getTs()).append(";cost=").append(super.naoToMs(runLine.getCost())).append("ms\n");
                sb.append("method=").append(runLine.getClazz()).append(".").append(runLine.getMethod()).append("\n");
                sb.append("param=").append(Strings.isNullOrEmpty(param) ? "" : param).append("\n");
                sb.append("return=").append(returnValue != null ? JsonUtil.toString(returnValue) : "Void");

                AgentCommand agentCommand = new AgentCommand();
                agentCommand.setTraceId(watchResult.getTraceId());
                agentCommand.setContent(sb.toString());
                TransmitSupport.getInstance().send(agentCommand);
            }
        }
    }

    @Override
    public CommandTypeEnum command() {
        return CommandTypeEnum.WATCH;
    }
}
