package com.jimmy.friday.center.action.gateway;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.Heartbeat;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.support.TransmitSupport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class GatewayHeartbeatAction implements Action<Heartbeat> {

    private final Map<Long, Heartbeat> result = Maps.newConcurrentMap();

    private final Map<Long, CountDownLatch> confirm = Maps.newConcurrentMap();

    @Autowired
    private TransmitSupport transmitSupport;

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

            transmitSupport.transmit(heartbeat, channel);
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
        return EventTypeEnum.GATEWAY_HEARTBEAT;
    }
}
