package com.jimmy.friday.center.action;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.ClientConnect;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ClientConnectAction implements Action<ClientConnect> {

    @Override
    public void action(ClientConnect clientConnect, ChannelHandlerContext channelHandlerContext) {
        String id = clientConnect.getId();

        log.info("成功连接，applicationId:{},目标ip:{}", id, channelHandlerContext.channel().remoteAddress().toString());

        ChannelHandlerPool.putChannel(channelHandlerContext.channel());
        ChannelHandlerPool.putSession(id, channelHandlerContext.channel().id());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CLIENT_CONNECT;
    }
}
