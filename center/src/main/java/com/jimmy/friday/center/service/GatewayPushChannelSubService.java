package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayPushChannelSub;

import java.util.List;

/**
 * (GatewayPushChannelSub)表服务接口
 *
 * @author makejava
 * @since 2024-02-19 11:37:51
 */
public interface GatewayPushChannelSubService extends IService<GatewayPushChannelSub> {

    Long getCurrentOffset(String channelName, String appId);

    void updateCurrentOffset(String channelName, String appId, Long currentOffset);

    void deleteCurrentOffset(String channelName, String appId);

    List<GatewayPushChannelSub> queryByChannelName(String channelName);

    GatewayPushChannelSub queryByAccountIdAndChannelName(Long accountId, String channelName);
}

