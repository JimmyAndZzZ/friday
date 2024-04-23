package com.jimmy.friday.client.netty.client;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.jimmy.friday.boot.message.ClientDisconnect;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyConnectorPool {

    private static final Map<String, NettyConnector> CONNECTOR_POOL = new ConcurrentHashMap<>();

    private static final String ID = IdUtil.simpleUUID();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (MapUtil.isNotEmpty(CONNECTOR_POOL)) {
                Collection<NettyConnector> values = CONNECTOR_POOL.values();
                for (NettyConnector value : values) {
                    ClientDisconnect clientDisconnect = new ClientDisconnect();
                    clientDisconnect.setId(ID);
                    value.send(clientDisconnect);
                }
            }
        }));
    }

    public static String getId() {
        return ID;
    }

    public static NettyConnector get(String server) {
        NettyConnector nettyConnector = new NettyConnector();

        NettyConnector put = CONNECTOR_POOL.putIfAbsent(server, nettyConnector);
        if (put != null) {
            return put;
        }

        nettyConnector.connect(server, ID);
        return nettyConnector;
    }
}
