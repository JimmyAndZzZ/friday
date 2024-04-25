package com.jimmy.friday.center.action;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.ClientDisconnect;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.gateway.ChannelSubManager;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ClientDisconnectAction implements Action<ClientDisconnect> {

    @Autowired
    private ChannelSubManager channelSubManager;

    @Override
    public void action(ClientDisconnect disconnect, ChannelHandlerContext channelHandlerContext) {
        String id = disconnect.getId();

        log.info("断开连接，applicationId:{},目标ip:{}", id, channelHandlerContext.channel().remoteAddress().toString());
        ChannelHandlerPool.removeSession(id);
        channelSubManager.removeChannels(id);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CLIENT_DISCONNECT;
    }
}
