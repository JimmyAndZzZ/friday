package com.jimmy.friday.client.netty.client;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.exception.ConnectionException;
import com.jimmy.friday.boot.exception.GatewayException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class NettyConnector {

    private final List<NettyClient> backup = new ArrayList<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private NettyClient master;

    public void connect(String server, String id) {
        if (running.compareAndSet(false, true)) {
            if (StrUtil.isEmpty(server)) {
                throw new GatewayException("未配置服务端地址或端口");
            }

            Set<String> repeat = new HashSet<>();
            String masterServer = server;
            if (StrUtil.contains(server, "backup")) {
                masterServer = StrUtil.subBefore(server, "?backup=", false);

                String s = StrUtil.subAfter(server, "?backup=", false);
                if (StrUtil.isNotEmpty(s)) {
                    List<String> split = StrUtil.split(s, ",");

                    for (String string : split) {
                        if (!string.equalsIgnoreCase(masterServer) && repeat.add(string)) {
                            NettyClient nettyClient = new NettyClient(string, id);
                            nettyClient.init();
                            this.backup.add(nettyClient);
                        }
                    }
                }
            }

            this.master = new NettyClient(masterServer, id);
            this.master.init();
        }
    }

    public void send(Message message) {
        if (master.getConnectSuccess()) {
            master.send(message);
            return;
        }

        if (CollUtil.isNotEmpty(backup)) {
            for (NettyClient nettyClient : backup) {
                if (nettyClient.getConnectSuccess()) {
                    nettyClient.send(message);
                    return;
                }
            }
        }

        throw new ConnectionException();
    }

    public void broadcast(Message message) {
        if (master.getConnectSuccess()) {
            master.send(message);
        }

        if (CollUtil.isNotEmpty(backup)) {
            for (NettyClient nettyClient : backup) {
                if (nettyClient.getConnectSuccess()) {
                    nettyClient.send(message);
                }
            }
        }
    }
}
