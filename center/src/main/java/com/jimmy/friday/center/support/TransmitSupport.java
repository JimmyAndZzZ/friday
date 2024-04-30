package com.jimmy.friday.center.support;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.exception.TransmitException;
import com.jimmy.friday.center.event.AckEvent;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.utils.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TransmitSupport implements ApplicationListener<AckEvent> {

    private final Map<String, AckTypeEnum> result = Maps.newConcurrentMap();

    private final Map<String, CountDownLatch> confirm = Maps.newConcurrentMap();

    @Override
    public void onApplicationEvent(AckEvent event) {
        String id = event.getId();
        CountDownLatch remove = confirm.remove(id);
        if (remove != null) {
            result.put(id, event.getAckType());
            remove.countDown();
        }
    }

    public void transmit(Message message, String channelId) {
        Channel channel = ChannelHandlerPool.getChannel(channelId);
        if (channel == null) {
            return;
        }

        this.transmit(message, channel);
    }

    public void transmit(Message message, ChannelOutboundInvoker channel) {
        if (message.type().getIsNeedAck()) {
            this.transmitWithAck(message, channel);
        } else {
            this.transmitWithoutAck(message, channel);
        }
    }

    public void transmitWithoutAck(Message message, ChannelOutboundInvoker channel) {
        channel.writeAndFlush(new Event(message.type(), JsonUtil.toString(message)));
    }

    public void transmitWithAck(Message message, ChannelOutboundInvoker channel) {
        Event event = new Event(message.type(), JsonUtil.toString(message));
        String id = event.getId();

        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            confirm.put(id, countDownLatch);

            channel.writeAndFlush(event);
            //等待回调
            countDownLatch.await(60, TimeUnit.SECONDS);

            if (countDownLatch.getCount() != 0L) {
                throw new TransmitException("消息确认超时");
            }
            //判断是否超时
            AckTypeEnum ackTypeEnum = result.remove(id);
            if (ackTypeEnum == null) {
                throw new TransmitException("消息确认结果为空");
            }

            if (ackTypeEnum.equals(AckTypeEnum.ERROR)) {
                throw new TransmitException("消息确认失败");
            }
        } catch (InterruptedException interruptedException) {
            throw new TransmitException("发送被中断");
        } finally {
            confirm.remove(id);
        }
    }
}
