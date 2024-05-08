package com.jimmy.friday.agent.command;

import com.google.common.base.Strings;
import com.jimmy.friday.agent.other.RunMonitorPool;
import com.jimmy.friday.agent.support.TransmitSupport;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.RunLine;
import com.jimmy.friday.boot.enums.agent.CommandTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentCommand;
import com.jimmy.friday.boot.result.TraceResult;

import java.util.List;

public class TraceWorker extends BaseWorker<RunLine> {

    @Override
    public void open(Command command) {
        RunMonitorPool.monitor(command);
    }

    @Override
    public void process(RunLine traceLine) {
        TraceResult traceResult = ContextHold.getTraceResult();
        if (traceResult != null) {
            traceLine.setCost(traceLine.getCost());
            traceResult.getRunLines().add(traceLine);
        }
    }

    @Override
    public void finish() {
        TraceResult traceResult = ContextHold.getTraceResult();
        if (traceResult != null) {
            ContextHold.removeTraceResult();

            StringBuilder sb = new StringBuilder();

            List<RunLine> runLines = traceResult.getRunLines();
            if (!runLines.isEmpty()) {
                for (int i = runLines.size() - 1; i >= 0; i--) {
                    RunLine runLine = runLines.get(i);

                    Long cost = runLine.getCost();
                    String param = runLine.getParam();

                    if (i == runLines.size() - 1) {
                        sb.append("---ts=").append(traceResult.getTs()).append(";thread_name=").append(traceResult.getThreadName()).append(";cost=").append(super.naoToMs(cost)).append("ms\n");
                    }

                    sb.append("  ").append("--[").append(super.naoToMs(cost)).append("] ").append(runLine.getClazz()).append(":").append(runLine.getMethod());
                    sb.append("(");
                    if (!Strings.isNullOrEmpty(param)) {
                        sb.append(param);
                    }

                    sb.append(")").append("\n");
                }
            }

            AgentCommand agentCommand = new AgentCommand();
            agentCommand.setTraceId(traceResult.getTraceId());
            agentCommand.setContent(sb.toString());
            TransmitSupport.getInstance().send(agentCommand);
        }
    }

    @Override
    public CommandTypeEnum command() {
        return CommandTypeEnum.TRACE;
    }
}
