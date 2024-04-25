package com.jimmy.friday.center.action.gateway;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.GatewayInvoke;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.gateway.GatewayInvokeFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class GatewayInvokeAction implements Action<GatewayInvoke> {

    @Override
    public void action(GatewayInvoke gatewayInvoke, ChannelHandlerContext channelHandlerContext) {
        CompletableFuture completableFuture = GatewayInvokeFuture.getAndClear(gatewayInvoke.getTraceId());
        if (completableFuture == null) {
            return;
        }
        //判断是否完成
        boolean complete = completableFuture.complete(gatewayInvoke);
        if (!complete) {
            log.error("rpc调用失败");
        }
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.GATEWAY_INVOKE;
    }
}
