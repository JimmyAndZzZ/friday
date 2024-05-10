package com.jimmy.friday.framework.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.exception.ConnectionException;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.exception.TransmitException;
import com.jimmy.friday.boot.other.ConfigConstants;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.netty.client.Client;
import com.jimmy.friday.framework.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TransmitSupport implements ApplicationContextAware {

    private final Map<String, CountDownLatch> confirm = Maps.newConcurrentMap();

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
        EventTypeEnum type = message.type();
        Event event = new Event(type, JsonUtil.toString(message));

        if (master.getConnectSuccess()) {
            if (type.getIsNeedAck()) {
                this.sendWithAck(event, master);
            } else {
                master.send(event);
            }

            return;
        }

        if (CollUtil.isNotEmpty(backup)) {
            for (Client client : backup) {
                if (client.getConnectSuccess()) {
                    if (type.getIsNeedAck()) {
                        this.sendWithAck(event, client);
                    } else {
                        client.send(event);
                    }

                    return;
                }
            }
        }

        throw new ConnectionException();
    }

    public void broadcast(Message message) {
        Event event = new Event(message.type(), JsonUtil.toString(message));

        if (master.getConnectSuccess()) {
            master.send(event);
        }

        if (CollUtil.isNotEmpty(backup)) {
            for (Client client : backup) {
                if (client.getConnectSuccess()) {
                    client.send(event);
                }
            }
        }
    }

    public void notify(String id) {
        CountDownLatch remove = confirm.remove(id);
        if (remove != null) {
            remove.countDown();
        }
    }

    /**
     * ack发送
     *
     * @param event
     * @param client
     */
    private void sendWithAck(Event event, Client client) {
        String id = event.getId();

        log.info("发了一个:{}", id);

        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            confirm.put(id, countDownLatch);

            client.send(event);
            //等待回调
            countDownLatch.await(60, TimeUnit.SECONDS);

            if (countDownLatch.getCount() != 0L) {
                throw new TransmitException("消息确认超时");
            }
        } catch (InterruptedException interruptedException) {
            throw new TransmitException("发送被中断");
        } finally {
            confirm.remove(id);
        }
    }
}
