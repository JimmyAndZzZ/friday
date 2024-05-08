package com.jimmy.friday.center.netty;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.center.support.ActionSupport;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class CenterEventHandler extends SimpleChannelInboundHandler<Event> {

    private final ActionSupport actionSupport;

    private final ExecutorService executorService;

    public CenterEventHandler(ActionSupport actionSupport, ExecutorService executorService) {
        this.actionSupport = actionSupport;
        this.executorService = executorService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Event event) throws Exception {
        log.debug("收到客户端推送:{}", event);
        try {
            executorService.submit(() -> actionSupport.action(event, ctx));
        } catch (RejectedExecutionException e) {
            log.error("Thread Pool Full");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //关闭与客户端的连接
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("事件处理异常", cause);
        ctx.close();
    }
}
