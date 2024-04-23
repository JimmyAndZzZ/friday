package com.jimmy.friday.agent.command;

import com.jimmy.friday.agent.support.TransmitSupport;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.enums.CommandTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentCommand;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

public class JvmWorker extends BaseWorker {

    @Override
    public void open(Command command) {
        StringBuilder sb = new StringBuilder("RUNTIME").append("\n");
        sb.append("--------------------------------------------------------------------------------------------------------------").append("\n");
        sb.append("CLASS-LOADING").append("\n");
        sb.append("--------------------------------------------------------------------------------------------------------------").append("\n");

        sb.append("LOADED-CLASS-COUNT: ").append(ManagementFactory.getClassLoadingMXBean().getLoadedClassCount()).append("\n");
        sb.append("TOTAL-LOADED-CLASS-COUNT: ").append(ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount()).append("\n");
        sb.append("UNLOADED-CLASS-COUNT: ").append(ManagementFactory.getClassLoadingMXBean().getUnloadedClassCount()).append("\n");

        sb.append("--------------------------------------------------------------------------------------------------------------").append("\n");
        sb.append("MEMORY").append("\n");
        sb.append("--------------------------------------------------------------------------------------------------------------").append("\n");

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        sb.append("HEAP-MEMORY-USAGE init: ").append(heapMemoryUsage.getInit()).append("(").append(this.byteToMb(heapMemoryUsage.getInit())).append(")").append("\n");
        sb.append("HEAP-MEMORY-USAGE used: ").append(heapMemoryUsage.getUsed()).append("(").append(this.byteToMb(heapMemoryUsage.getUsed())).append(")").append("\n");
        sb.append("HEAP-MEMORY-USAGE committed: ").append(heapMemoryUsage.getCommitted()).append(this.byteToMb(heapMemoryUsage.getCommitted())).append(")").append("(").append("\n");
        sb.append("HEAP-MEMORY-USAGE max: ").append(heapMemoryUsage.getMax()).append("(").append(this.byteToMb(heapMemoryUsage.getMax())).append(")").append("\n");
        sb.append("\n");

        sb.append("NO-HEAP-MEMORY-USAGE init: ").append(nonHeapMemoryUsage.getInit()).append("(").append(this.byteToMb(nonHeapMemoryUsage.getInit())).append(")").append("\n");
        sb.append("NO-HEAP-MEMORY-USAGE used: ").append(nonHeapMemoryUsage.getUsed()).append("(").append(this.byteToMb(nonHeapMemoryUsage.getUsed())).append(")").append("\n");
        sb.append("NO-HEAP-MEMORY-USAGE committed: ").append(nonHeapMemoryUsage.getCommitted()).append("(").append(this.byteToMb(nonHeapMemoryUsage.getCommitted())).append(")").append("\n");
        sb.append("NO-HEAP-MEMORY-USAGE max: ").append(nonHeapMemoryUsage.getMax()).append("(").append(this.byteToMb(nonHeapMemoryUsage.getMax())).append(")").append("\n");
        sb.append("\n");

        sb.append("--------------------------------------------------------------------------------------------------------------").append("\n");
        sb.append("THREAD").append("\n");
        sb.append("--------------------------------------------------------------------------------------------------------------").append("\n");

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadMXBean.getThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        int deadlockedThreadCount = deadlockedThreads != null ? deadlockedThreads.length : 0;

        sb.append("线程总数: ").append(threadCount).append("\n");
        sb.append("线程峰值数: ").append(peakThreadCount).append("\n");
        sb.append("线程启动数: ").append(totalStartedThreadCount).append("\n");
        sb.append("死锁线程数: ").append(deadlockedThreadCount).append("\n");

        AgentCommand agentCommand = new AgentCommand();
        agentCommand.setTraceId(command.getTraceId());
        agentCommand.setContent(sb.toString());
        TransmitSupport.getInstance().send(agentCommand);
    }

    /**
     * byte转Mb
     *
     * @param bytes
     * @return
     */
    private double byteToMb(long bytes) {
        return (double) bytes / (1024 * 1024);
    }

    @Override
    public CommandTypeEnum command() {
        return CommandTypeEnum.JVM;
    }
}
