package com.jimmy.friday.framework.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.exception.ConnectionException;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.ConfigConstants;
import com.jimmy.friday.framework.netty.client.Client;
import com.jimmy.friday.framework.core.ConfigLoad;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransmitSupport implements ApplicationContextAware {

    private final AtomicBoolean running = new AtomicBoolean(false);

    private Client master;

    private ConfigLoad configLoad;

    private ApplicationContext applicationContext;

    private final List<Client> backup = new ArrayList<>();

    public TransmitSupport(ConfigLoad configLoad) {
        this.configLoad = configLoad;
    }

    public void init() {
        if (running.compareAndSet(false, true)) {
            String server = configLoad.get(ConfigConstants.COLLECTOR_SERVER);
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
                            Client client = new Client(applicationContext, string);
                            client.init(this.configLoad);
                            this.backup.add(client);
                        }
                    }
                }
            }

            this.master = new Client(applicationContext, masterServer);
            this.master.init(this.configLoad);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void send(Message message) {
        if (master.getConnectSuccess()) {
            master.send(message);
            return;
        }

        if (CollUtil.isNotEmpty(backup)) {
            for (Client client : backup) {
                if (client.getConnectSuccess()) {
                    client.send(message);
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
            for (Client client : backup) {
                if (client.getConnectSuccess()) {
                    client.send(message);
                }
            }
        }
    }
}
