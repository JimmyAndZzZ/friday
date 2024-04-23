package com.jimmy.friday.center.action.gateway;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.Heartbeat;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class HeartbeatAction implements Action<Heartbeat> {

    private final Map<Long, Heartbeat> result = Maps.newConcurrentMap();

    private final Map<Long, CountDownLatch> confirm = Maps.newConcurrentMap();

    @Override
    public void action(Heartbeat heartbeat, ChannelHandlerContext channelHandlerContext) {
        Long traceId = heartbeat.getTraceId();
        CountDownLatch countDownLatch = this.confirm.get(traceId);
        if (countDownLatch != null) {
            this.confirm.remove(traceId);
            this.result.put(traceId, heartbeat);
            countDownLatch.countDown();
        }
    }

    public Heartbeat heartbeat(Heartbeat heartbeat, String applicationId) {
        Long traceId = heartbeat.getTraceId();
        try {
            //阻塞
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.confirm.put(traceId, countDownLatch);

            Channel channel = ChannelHandlerPool.getChannel(applicationId);
            if (channel == null) {
                return null;
            }

            channel.writeAndFlush(new Event(EventTypeEnum.HEARTBEAT, JSON.toJSONString(heartbeat)));

            countDownLatch.await(60, TimeUnit.SECONDS);
            //等待超时
            if (countDownLatch.getCount() != 0L) {
                this.confirm.remove(traceId);
                return null;
            }

            return this.result.remove(traceId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.HEARTBEAT;
    }
}
