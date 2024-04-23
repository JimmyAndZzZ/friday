package com.jimmy.friday.center.support;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentHeartbeat;
import com.jimmy.friday.boot.message.agent.AgentRegister;
import com.jimmy.friday.center.event.LoseConnectionEvent;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class AgentSupport implements ApplicationListener<LoseConnectionEvent> {

    private final Map<String, AtomicInteger> timeoutCountMap = Maps.newHashMap();

    private final Map<Long, CountDownLatch> countDownLatchMap = Maps.newHashMap();

    private final Map<String, AgentRegister> registerMessageMap = Maps.newHashMap();

    private final Map<String, ScheduledExecutorService> executorServiceHashMap = Maps.newHashMap();

    @Autowired
    private RemindSupport remindSupport;

    @Autowired
    private ApplicationContext applicationContext;

    public Set<String> getAppList() {
        return registerMessageMap.keySet();
    }

    public void createHeartbeat(AgentRegister registerMessage, Channel channel) {
        String ip = registerMessage.getIp();
        String name = registerMessage.getName();
        Integer heartbeatTimeout = registerMessage.getHeartbeatTimeout();
        Integer heartbeatInterval = registerMessage.getHeartbeatInterval();

        log.info("接收到心跳注册信息,ip:{},name:{}", ip, name);
        //删除原有
        this.remove(name, ip);

        String key = name + "&&" + ip;

        ChannelHandlerPool.putChannel(channel);
        ChannelHandlerPool.putSession(key, channel.id());

        this.timeoutCountMap.put(key, new AtomicInteger(0));
        this.registerMessageMap.put(key, registerMessage);
        //保存线程池信息
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        this.executorServiceHashMap.put(key, executor);

        executor.scheduleAtFixedRate(() -> {
            Long traceId = IdUtil.getSnowflake(1, 1).nextId();
            try {
                CountDownLatch downLatch = new CountDownLatch(1);
                this.countDownLatchMap.put(traceId, downLatch);
                //发送心跳包
                Event event = new Event(EventTypeEnum.AGENT_HEARTBEAT, JSON.toJSONString(new AgentHeartbeat(traceId)));
                channel.writeAndFlush(event);

                downLatch.await(heartbeatTimeout, TimeUnit.SECONDS);
                if (downLatch.getCount() != 0L) {
                    log.error("{},{},心跳响应超时", name, ip);
                    int i = timeoutCountMap.get(key).incrementAndGet();
                    if (i > 5) {
                        LoseConnectionEvent loseConnectionEvent = new LoseConnectionEvent(applicationContext);
                        loseConnectionEvent.setIp(ip);
                        loseConnectionEvent.setName(name);
                        applicationContext.publishEvent(loseConnectionEvent);
                    }

                    return;
                }
            } catch (InterruptedException e) {
                log.error("阻塞被中断");
            } finally {
                countDownLatchMap.remove(traceId);
            }
        }, 0L, new Long(heartbeatInterval), TimeUnit.SECONDS);
    }

    public void release(AgentHeartbeat heartbeatMessage) {
        String ip = heartbeatMessage.getIp();
        String name = heartbeatMessage.getName();
        Long traceId = heartbeatMessage.getTraceId();
        //接触阻塞
        CountDownLatch countDownLatch = countDownLatchMap.get(traceId);
        if (countDownLatch != null) {
            countDownLatch.countDown();
            countDownLatchMap.remove(traceId);
        }

        this.timeoutCountMap.put(name + "&&" + ip, new AtomicInteger(0));
    }

    public void remove(String name, String ip) {
        String key = name + "&&" + ip;

        ScheduledExecutorService scheduledExecutorService = this.executorServiceHashMap.get(key);
        if (scheduledExecutorService != null) {
            this.executorServiceHashMap.remove(key);
            scheduledExecutorService.shutdown();
        }

        this.timeoutCountMap.remove(key);
        this.registerMessageMap.remove(key);

        Channel channel = ChannelHandlerPool.getChannel(key);
        if (channel != null) {
            ChannelHandlerPool.removeSession(key);
            ChannelHandlerPool.closeChannel(channel);
        }

        remindSupport.remind(name + "服务心跳异常，服务退出登录", "ip：" + ip + ",name：" + name + "该程序服务心跳异常,服务退出登录");
    }

    @Override
    public void onApplicationEvent(LoseConnectionEvent event) {
        this.remove(event.getName(), event.getIp());
    }
}
