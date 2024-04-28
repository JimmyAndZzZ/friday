package com.jimmy.friday.center.core.gateway;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.utils.LockKeyConstants;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class ChannelSubManager {

    private final Map<String, String> channelMapper = Maps.newConcurrentMap();

    private final Map<String, Set<String>> subChannels = Maps.newConcurrentMap();

    @Autowired
    private StripedLock stripedLock;

    public Channel getChannel(String appId) {
        Set<String> strings = subChannels.get(appId);
        if (CollUtil.isEmpty(strings)) {
            return null;
        }

        for (String string : strings) {
            Channel channel = ChannelHandlerPool.getChannel(string);
            if (channel != null && channel.isOpen() && channel.isActive()) {
                return channel;
            }
        }

        return null;
    }

    public void putChannels(String appId, String name) {
        Lock lock = stripedLock.getLocalLock(LockKeyConstants.Gateway.GATEWAY_CHANNEL_SUB, 16, appId + ":" + name);

        lock.lock();

        try {
            Set<String> put = subChannels.putIfAbsent(appId, Sets.newHashSet(name));
            if (put != null) {
                put.add(name);
            }

            channelMapper.put(name, appId);
        } finally {
            lock.unlock();
        }
    }

    public void removeChannels(String name) {
        String appId = channelMapper.remove(name);
        if (StrUtil.isEmpty(appId)) {
            return;
        }

        Lock lock = stripedLock.getLocalLock(LockKeyConstants.Gateway.GATEWAY_CHANNEL_SUB, 16, appId + ":" + name);

        lock.lock();

        try {
            Set<String> strings = subChannels.get(appId);
            if (CollUtil.isNotEmpty(strings)) {
                strings.remove(name);
            }
        } finally {
            lock.unlock();
        }
    }

}
