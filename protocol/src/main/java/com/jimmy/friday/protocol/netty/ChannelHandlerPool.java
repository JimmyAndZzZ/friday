package com.jimmy.friday.protocol.netty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.List;
import java.util.Map;

public class ChannelHandlerPool {

    private ChannelHandlerPool() {
    }

    private static ChannelGroup globalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static Map<String, ChannelId> userSession = Maps.newHashMap();

    public static void putChannel(Channel channel) {
        globalGroup.add(channel);
    }

    public static void closeChannel(Channel channel) {
        globalGroup.remove(channel);
    }

    public static List<Channel> allChannel() {
        return Lists.newArrayList(globalGroup);
    }

    public static void putSession(String userId, ChannelId id) {
        userSession.put(userId, id);
    }

    public static Channel getChannel(String userId) {
        ChannelId s = userSession.get(userId);
        if (s == null) {
            return null;
        }

        return globalGroup.find(s);
    }

}
