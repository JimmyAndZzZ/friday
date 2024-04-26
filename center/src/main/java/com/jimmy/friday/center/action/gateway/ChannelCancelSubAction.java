package com.jimmy.friday.center.action.gateway;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelCancelSub;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.entity.GatewayAccount;
import com.jimmy.friday.center.entity.GatewayPushChannelSub;
import com.jimmy.friday.center.service.GatewayAccountService;
import com.jimmy.friday.center.service.GatewayPushChannelSubService;
import com.jimmy.friday.center.core.gateway.support.ChannelSupport;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChannelCancelSubAction implements Action<ChannelCancelSub> {

    @Autowired
    private ChannelSupport channelSupport;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Autowired
    private GatewayPushChannelSubService gatewayPushChannelSubService;

    @Override
    public void action(ChannelCancelSub channelCancelSub, ChannelHandlerContext channelHandlerContext) {
        String appId = channelCancelSub.getAppId();
        String channelName = channelCancelSub.getChannelName();

        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        if (gatewayAccount == null) {
            log.error("{}该appId不存在", appId);
            return;
        }

        GatewayPushChannelSub gatewayPushChannelSub = gatewayPushChannelSubService.queryByAccountIdAndChannelName(gatewayAccount.getId(), channelName);
        if (gatewayPushChannelSub == null) {
            log.error("订阅信息不存在,appId:{},channelName:{}", appId, channelName);
            return;
        }

        log.info("取消订阅,appId:{},channelName:{}", appId, channelName);

        gatewayPushChannelSubService.deleteCurrentOffset(channelName, appId);

        gatewayPushChannelSubService.removeById(gatewayPushChannelSub.getId());
        channelSupport.stop(gatewayPushChannelSub, appId);

        gatewayPushChannelSubService.deleteCurrentOffset(channelName, appId);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_CANCEL_SUB;
    }
}
