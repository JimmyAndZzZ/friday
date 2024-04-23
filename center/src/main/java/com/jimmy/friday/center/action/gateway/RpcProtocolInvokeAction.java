package com.jimmy.friday.center.action.gateway;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.RpcProtocolInvoke;
import com.jimmy.friday.center.Gateway;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RpcProtocolInvokeAction implements Action<RpcProtocolInvoke> {

    @Autowired
    private Gateway gateway;

    @Override
    public void action(RpcProtocolInvoke rpcProtocolInvoke, ChannelHandlerContext channelHandlerContext) {
        try {
            GatewayResponse gatewayResponse = gateway.run(rpcProtocolInvoke.getGatewayRequest());
            rpcProtocolInvoke.setGatewayResponse(gatewayResponse);
            channelHandlerContext.writeAndFlush(new Event(type(), JsonUtil.toString(rpcProtocolInvoke)));
        } catch (Throwable e) {
            GatewayResponse gatewayResponse = new GatewayResponse();
            gatewayResponse.setError(e.getMessage());
            gatewayResponse.setIsSuccess(false);
            rpcProtocolInvoke.setGatewayResponse(gatewayResponse);
            channelHandlerContext.writeAndFlush(new Event(type(), JsonUtil.toString(rpcProtocolInvoke)));
        }
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.RPC_PROTOCOL_INVOKE;
    }
}
