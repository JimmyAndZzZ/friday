package com.jimmy.friday.center.action.gateway;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelSub;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.ChannelSubManager;
import com.jimmy.friday.center.entity.GatewayAccount;
import com.jimmy.friday.center.service.GatewayAccountService;
import com.jimmy.friday.center.support.ChannelSupport;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChannelSubAction implements Action<ChannelSub> {

    @Autowired
    private ChannelSupport channelSupport;

    @Autowired
    private ChannelSubManager channelSubManager;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Override
    public void action(ChannelSub channelSub, ChannelHandlerContext channelHandlerContext) {
        String appId = channelSub.getAppId();
        String channelName = channelSub.getChannelName();
        String applicationId = channelSub.getApplicationId();

        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        if (gatewayAccount == null) {
            log.error("{}该appId不存在", appId);
            return;
        }

        log.info("开启订阅,appId:{},channelName:{}", appId, channelName);

        channelSubManager.putChannels(appId, applicationId);
        channelSupport.register(appId, channelName);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_SUB;
    }
}
