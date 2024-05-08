package com.jimmy.friday.client.netty.client;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.Ack;
import com.jimmy.friday.boot.message.ClientConnect;
import com.jimmy.friday.boot.message.gateway.ChannelSub;
import com.jimmy.friday.client.base.Handler;
import com.jimmy.friday.client.handler.ChannelReceiveHandler;
import com.jimmy.friday.client.handler.InvokeCallbackHandler;
import com.jimmy.friday.client.handler.RpcInvokeHandler;
import com.jimmy.friday.client.support.ChannelSupport;
import com.jimmy.friday.client.utils.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<Event> {

    private final Map<EventTypeEnum, Handler> handlerMap = new HashMap<>();

    private final String id;

    private final NettyClient nettyClient;

    public NettyClientHandler(NettyClient nettyClient, String id) {
        super();

        this.id = id;
        this.nettyClient = nettyClient;
        this.handlerMap.put(EventTypeEnum.RPC_PROTOCOL_INVOKE, new RpcInvokeHandler());
        this.handlerMap.put(EventTypeEnum.INVOKE_CALLBACK, new InvokeCallbackHandler());
        this.handlerMap.put(EventTypeEnum.CHANNEL_RECEIVE, new ChannelReceiveHandler());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端连接
        ClientConnect clientConnect = new ClientConnect();
        clientConnect.setId(id);
        ctx.writeAndFlush(new Event(EventTypeEnum.CLIENT_CONNECT, JsonUtil.toString(clientConnect)));

        List<ChannelSub> channelSubList = ChannelSupport.getChannelSubList();
        if (CollUtil.isNotEmpty(channelSubList)) {
            for (ChannelSub channelSub : channelSubList) {
                ctx.writeAndFlush(new Event(EventTypeEnum.CHANNEL_SUB, JsonUtil.toString(channelSub)));
            }
        }

        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Event event) throws Exception {
        String type = event.getType();

        ctx.executor().execute(() -> {
            EventTypeEnum eventTypeEnum = EventTypeEnum.queryByCode(type);
            if (eventTypeEnum == null) {
                return;
            }

            if (eventTypeEnum.getIsNeedAck()) {
                Ack ack = new Ack();
                ack.setId(event.getId());
                ctx.writeAndFlush(new Event(EventTypeEnum.ACK, JsonUtil.toString(ack)));
            }

            Handler handler = handlerMap.get(eventTypeEnum);
            if (handler != null) {
                handler.handler(event, ctx);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        nettyClient.connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
