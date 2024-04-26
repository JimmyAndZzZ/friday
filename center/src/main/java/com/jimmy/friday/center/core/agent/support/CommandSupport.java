package com.jimmy.friday.center.core.agent.support;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.CommandTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.agent.AgentCommand;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.center.base.Handler;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.agent.CommandSession;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.other.CommandParser;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CommandSupport implements Initialize {

    private final Map<Long, AgentCommand> result = Maps.newConcurrentMap();

    private final Map<Long, CountDownLatch> notify = Maps.newConcurrentMap();

    private final Map<CommandTypeEnum, Handler> handlerMap = Maps.newHashMap();

    @Autowired
    private CommandSession commandSession;

    @Autowired
    private ApplicationContext applicationContext;

    public void notify(AgentCommand agentCommand) {
        Long traceId = agentCommand.getTraceId();

        CountDownLatch countDownLatch = this.notify.get(traceId);
        if (countDownLatch == null) {
            log.error("回调为空,traceId:{}", traceId);
            return;
        }

        this.result.put(traceId, agentCommand);
        countDownLatch.countDown();
    }

    public String execute(String command, String sessionKey) {
        Long traceId = IdUtil.getSnowflake(1, 1).nextId();
        //解析
        Command cmd = CommandParser.parseCommand(command, traceId);

        CommandTypeEnum commandTypeEnum = CommandTypeEnum.queryByCmd(cmd.getCommand());
        if (commandTypeEnum == null) {
            return StrUtil.format("-bash: {}: command not found", cmd.getCommand());
        }

        try {
            String current = commandSession.getCurrent(sessionKey);

            StringBuilder sb = new StringBuilder("[").append(current).append("]#").append(StrUtil.SPACE);

            Handler handler = handlerMap.get(commandTypeEnum);
            if (handler != null) {
                return handler.isCover() ? handler.execute(cmd, sessionKey) : sb.append(handler.execute(cmd, sessionKey)).toString();
            }

            if (GlobalConstants.Center.ROOT.equalsIgnoreCase(current)) {
                throw new GatewayException("请先选择应用");
            }

            Channel channel = ChannelHandlerPool.getChannel(current);
            if (channel == null) {
                return sb.append("应用未注册").toString();
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);
            notify.put(traceId, countDownLatch);

            AgentCommand agentCommand = new AgentCommand();
            agentCommand.setCommand(cmd.getCommand());
            agentCommand.setTraceId(traceId);
            agentCommand.setParam(cmd.getParam());
            channel.writeAndFlush(new Event(EventTypeEnum.AGENT_COMMAND, JSON.toJSONString(agentCommand)));

            countDownLatch.await(120, TimeUnit.SECONDS);
            //等待超时
            if (countDownLatch.getCount() != 0L) {
                return sb.append("执行超时").toString();
            }
            //获取客户端返回值
            AgentCommand result = this.result.get(traceId);
            if (result == null) {
                return sb.append("客户端响应为空").toString();
            }

            this.result.remove(traceId);
            this.notify.remove(traceId);
            return sb.append(result.getIsSuccess() ? result.getContent() : "执行失败:" + result.getErrorMessage()).toString();
        } catch (GatewayException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("执行失败", e);
            return e.getMessage();
        } finally {
            this.notify.remove(traceId);
        }
    }

    @Override
    public void init() throws Exception {
        Map<String, Handler> beansOfType = applicationContext.getBeansOfType(Handler.class);
        beansOfType.values().forEach(bean -> this.handlerMap.put(bean.type(), bean));
    }

    @Override
    public int sort() {
        return 0;
    }
}
