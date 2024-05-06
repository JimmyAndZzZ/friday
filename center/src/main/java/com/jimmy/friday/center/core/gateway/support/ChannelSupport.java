package com.jimmy.friday.center.core.gateway.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.ChannelReceive;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.gateway.ChannelSubManager;
import com.jimmy.friday.center.core.KafkaManager;
import com.jimmy.friday.center.entity.GatewayAccount;
import com.jimmy.friday.center.entity.GatewayPushChannelSub;
import com.jimmy.friday.center.event.ReceiveConfirmEvent;
import com.jimmy.friday.center.service.GatewayAccountService;
import com.jimmy.friday.center.service.GatewayPushChannelSubService;
import com.jimmy.friday.center.utils.JsonUtil;
import com.jimmy.friday.protocol.core.InputMessage;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChannelSupport implements Initialize, ApplicationListener<ReceiveConfirmEvent> {

    private static final String TOPIC_FORMAT = "gateway_push_{}_{}";

    private final Set<String> topics = Sets.newConcurrentHashSet();

    private final Map<Long, CountDownLatch> countDownLatchMap = Maps.newConcurrentMap();

    private final Map<Long, ReceiveConfirmEvent> confirmEventMap = Maps.newConcurrentMap();

    @Autowired
    private KafkaManager kafkaManager;

    @Autowired
    private ChannelSubManager channelSubManager;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Autowired
    private GatewayPushChannelSubService gatewayPushChannelSubService;

    public void stop(GatewayPushChannelSub gatewayPushChannelSub, String appId) {
        String topic = this.getTopic(gatewayPushChannelSub);

        if (topics.remove(topic)) {
            kafkaManager.close(topic, appId);
        }
    }

    public void push(String message, String channelName) {
        List<GatewayPushChannelSub> gatewayPushChannelSubs = gatewayPushChannelSubService.queryByChannelName(channelName);
        if (CollUtil.isNotEmpty(gatewayPushChannelSubs)) {
            for (GatewayPushChannelSub gatewayPushChannelSub : gatewayPushChannelSubs) {
                String topic = this.getTopic(gatewayPushChannelSub);
                if (StrUtil.isEmpty(topic)) {
                    continue;
                }

                ChannelMessage channelMessage = new ChannelMessage();
                channelMessage.setMessage(message);
                channelMessage.setId(IdUtil.getSnowflake(1, 1).nextId());

                kafkaManager.send(topic, JsonUtil.toString(channelMessage));
            }
        }
    }

    public void register(String appId, String channelName) {
        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        if (gatewayAccount == null) {
            throw new GatewayException("appId不存在");
        }

        GatewayPushChannelSub gatewayPushChannelSub = new GatewayPushChannelSub();
        gatewayPushChannelSub.setChannelName(channelName);
        gatewayPushChannelSub.setAccountId(gatewayAccount.getId());
        boolean save = gatewayPushChannelSubService.save(gatewayPushChannelSub);

        if (save) {
            this.receive(gatewayPushChannelSub, appId);
        }
    }

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        List<GatewayPushChannelSub> list = gatewayPushChannelSubService.list();
        if (CollUtil.isNotEmpty(list)) {
            Map<Long, List<GatewayPushChannelSub>> collect = list.stream().collect(Collectors.groupingBy(GatewayPushChannelSub::getAccountId));

            for (Map.Entry<Long, List<GatewayPushChannelSub>> entry : collect.entrySet()) {
                Long accountId = entry.getKey();
                List<GatewayPushChannelSub> value = entry.getValue();

                String appId = gatewayAccountService.getAppIdById(accountId);
                if (StrUtil.isEmpty(appId)) {
                    log.error("id:{}账号不存在", accountId);
                    continue;
                }

                for (GatewayPushChannelSub gatewayPushChannelSub : value) {
                    this.receive(gatewayPushChannelSub, appId);
                }
            }
        }
    }

    @Override
    public int sort() {
        return 1;
    }

    @Override
    public void onApplicationEvent(ReceiveConfirmEvent event) {
        Long id = event.getId();
        CountDownLatch countDownLatch = countDownLatchMap.remove(id);
        if (countDownLatch != null) {
            confirmEventMap.put(id, event);
            countDownLatch.countDown();
        }
    }

    /**
     * 阻塞等待
     *
     * @param id
     */
    private void await(Long id) {
        CountDownLatch countDownLatch = countDownLatchMap.get(id);
        if (countDownLatch == null) {
            throw new GatewayException("未注册ACK任务");
        }

        try {
            countDownLatch.await(120, TimeUnit.SECONDS);
            //等待超时
            if (countDownLatch.getCount() != 0L) {
                throw new GatewayException("等待ACK超时");
            }

            ReceiveConfirmEvent receiveConfirmEvent = confirmEventMap.get(id);
            if (receiveConfirmEvent == null) {
                throw new GatewayException("ACK响应为空");
            }

            AckTypeEnum ackType = receiveConfirmEvent.getAckType();

            if (Objects.requireNonNull(ackType) == AckTypeEnum.ERROR) {
                throw new GatewayException(StrUtil.emptyToDefault(receiveConfirmEvent.getErrorMessage(), "ACK失败"));
            }
        } catch (InterruptedException interruptedException) {
            throw new GatewayException("等待ACK被中断");
        } finally {
            countDownLatchMap.remove(id);
            confirmEventMap.remove(id);
        }
    }

    /**
     * 发送消息
     *
     * @param message
     * @param channel
     */
    private void send(InputMessage message, Channel channel, GatewayPushChannelSub gatewayPushChannelSub, String appId) {
        try {
            String body = message.getMessage();
            long offset = message.getOffset();
            String channelName = gatewayPushChannelSub.getChannelName();

            Long currentOffset = gatewayPushChannelSubService.getCurrentOffset(channelName, appId);
            if (currentOffset != null && currentOffset >= offset && (currentOffset != 0L && offset != 0L)) {
                log.info("channel:{},appId:{},当前offset已被处理, lastOffset:{},now:{}", channelName, appId, currentOffset, offset);
                return;
            }

            ChannelMessage channelMessage = JsonUtil.parseObject(body, ChannelMessage.class);

            Long id = channelMessage.getId();

            ChannelReceive channelReceive = new ChannelReceive();
            channelReceive.setId(id);
            channelReceive.setChannelName(channelName);
            channelReceive.setOffset(offset);
            channelReceive.setMessage(channelMessage.getMessage());
            channelReceive.setAppId(appId);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatchMap.put(id, countDownLatch);

            channel.writeAndFlush(new Event(EventTypeEnum.CHANNEL_RECEIVE, JsonUtil.toString(channelReceive)));

            this.await(id);

            gatewayPushChannelSubService.updateCurrentOffset(channelName, appId, offset);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("推送消息失败", e);
            throw e;
        }
    }

    /**
     * 接收消息
     */
    private void receive(GatewayPushChannelSub gatewayPushChannelSub, String appId) {
        String topic = this.getTopic(gatewayPushChannelSub);
        if (StrUtil.isEmpty(topic)) {
            return;
        }

        if (topics.add(topic)) {
            kafkaManager.receive(topic, appId, 100, message -> {
                GatewayPushChannelSub byId = gatewayPushChannelSubService.getById(gatewayPushChannelSub.getId());
                if (byId == null) {
                    return;
                }

                String body = message.getMessage();

                log.info("消费推送消息:{}", body);

                Channel channel = channelSubManager.getChannel(appId);
                if (channel == null) {
                    log.error("appId:{}未连接", appId);

                    ThreadUtil.sleep(GlobalConstants.Client.PUSH_MESSAGE_WAIT_TIME * 1000);

                    throw new GatewayException(appId + "用户不在线");
                }

                send(message, channel, gatewayPushChannelSub, appId);
            });
        }
    }

    /**
     * 获取topic的名称
     *
     * @param gatewayPushChannelSub
     * @return
     */
    private String getTopic(GatewayPushChannelSub gatewayPushChannelSub) {
        Long accountId = gatewayPushChannelSub.getAccountId();
        String channelName = gatewayPushChannelSub.getChannelName();

        String appId = gatewayAccountService.getAppIdById(accountId);
        if (StrUtil.isEmpty(appId)) {
            log.error("id:{}账号不存在", accountId);
            return null;
        }

        return StrUtil.format(TOPIC_FORMAT, appId, channelName);
    }

    @Data
    private static class ChannelMessage implements Serializable {

        private Long id;

        private String message;
    }
}
