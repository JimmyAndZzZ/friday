package com.jimmy.friday.center.netty;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ChannelHandlerPool {

    private static final Map<String, ChannelId> SESSION = Maps.newConcurrentMap();

    private static final ChannelGroup GLOBAL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private ChannelHandlerPool() {
    }

    public static void putChannel(Channel channel) {
        GLOBAL_GROUP.add(channel);
    }

    public static void closeChannel(Channel channel) {
        GLOBAL_GROUP.remove(channel);
    }

    public static void close() {
        GLOBAL_GROUP.close().syncUninterruptibly();
    }

    public static void putSession(String name, ChannelId id) {
        SESSION.put(name, id);
    }

    public static void removeSession(String name) {
        ChannelId remove = SESSION.remove(name);
        if (remove != null) {
            Channel channel = GLOBAL_GROUP.find(remove);
            if (channel != null) {
                closeChannel(channel);
            }
        }
    }

    public static Channel getChannel(String name) {
        ChannelId s = SESSION.get(name);
        if (s == null) {
            return null;
        }

        return GLOBAL_GROUP.find(s);
    }
}
