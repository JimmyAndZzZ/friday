package com.jimmy.friday.framework.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.base.Listen;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.*;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.framework.core.ConfigLoad;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChannelSupport {

    private final Map<String, ChannelSubInfo> infoMap = new ConcurrentHashMap<>();

    private final Map<Long, ChannelPushConfirm> confirmEventMap = new ConcurrentHashMap<>();

    private final Map<Long, CountDownLatch> countDownLatchMap = new ConcurrentHashMap<>();

    private final ExecutorService executorService = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private ConfigLoad configLoad;

    private TransmitSupport transmitSupport;

    public ChannelSupport(ConfigLoad configLoad, TransmitSupport transmitSupport) {
        this.configLoad = configLoad;
        this.transmitSupport = transmitSupport;
    }

    public List<ChannelSub> getChannelSubList() {
        return MapUtil.isEmpty(infoMap) ? new ArrayList<>() : infoMap.values().stream().map(ChannelSubInfo::getChannelSub).collect(Collectors.toList());
    }

    public void cancelSub(String name, String appId) {
        if (StrUtil.isEmpty(appId)) {
            throw new GatewayException("appId为空");
        }

        if (StrUtil.isEmpty(name)) {
            throw new GatewayException("channel为空");
        }

        String key = appId + ":" + name;
        infoMap.remove(key);

        FileUtil.del(this.getOffsetFile(name, appId));

        ChannelCancelSub channelCancelSub = new ChannelCancelSub();
        channelCancelSub.setAppId(appId);
        channelCancelSub.setChannelName(name);
        transmitSupport.send(channelCancelSub);

        FileUtil.del(this.getOffsetFile(name, appId));
    }

    public void sub(String name, String appId, Listen listen) {
        if (StrUtil.isEmpty(appId)) {
            throw new GatewayException("appId为空");
        }

        if (StrUtil.isEmpty(name)) {
            throw new GatewayException("channel为空");
        }

        String key = appId + ":" + name;

        if (infoMap.containsKey(key)) {
            throw new GatewayException("已订阅");
        }

        if (!FileUtil.exist(this.getOffsetDir(name))) {
            FileUtil.mkdir(this.getOffsetDir(name));
        }

        ChannelSub channelSub = new ChannelSub();
        channelSub.setAppId(appId);
        channelSub.setChannelName(name);
        channelSub.setApplicationId(configLoad.getId());

        ChannelSubInfo channelSubInfo = new ChannelSubInfo();
        channelSubInfo.setListen(listen);
        channelSubInfo.setChannelSub(channelSub);

        ChannelSubInfo put = infoMap.putIfAbsent(key, channelSubInfo);
        if (put != null) {
            throw new GatewayException("已订阅");
        }

        transmitSupport.send(channelSub);
    }

    public void receive(ChannelReceive channelReceive) {
        try {
            String appId = channelReceive.getAppId();
            Long offset = channelReceive.getOffset();
            String message = channelReceive.getMessage();
            String channelName = channelReceive.getChannelName();

            if (MapUtil.isEmpty(infoMap)) {
                return;
            }

            Collection<ChannelSubInfo> values = infoMap.values();

            for (ChannelSubInfo value : values) {
                Listen listen = value.getListen();
                String subAppId = value.getChannelSub().getAppId();
                String subChannelName = value.getChannelSub().getChannelName();

                if (!subChannelName.equals(channelName) || !subAppId.equals(appId)) {
                    continue;
                }

                Long lastOffset = this.getOffset(channelName, appId);
                if (lastOffset != null && lastOffset >= offset && (lastOffset != 0L && offset != 0L)) {
                    log.info("channel:{},appId:{},当前offset已被处理, lastOffset:{},now:{}", channelName, appId, lastOffset, offset);
                    continue;
                }

                this.writeOffset(offset, channelName, appId);
                executorService.submit(() -> listen.listen(message));
            }

        } catch (Exception e) {
            log.error("推送消息处理失败", e);
        }
    }

    public void push(Long id, String message) {
        if (StrUtil.isEmpty(message)) {
            throw new GatewayException("消息体为空");
        }

        try {
            ChannelPush channelPush = new ChannelPush();
            channelPush.setId(id);
            channelPush.setMessage(message);
            channelPush.setServiceName(configLoad.getApplicationName());

            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatchMap.put(id, countDownLatch);

            transmitSupport.send(channelPush);

            this.await(id);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("推送消息失败", e);
            throw e;
        }
    }

    public void notify(ChannelPushConfirm channelPushConfirm) {
        Long id = channelPushConfirm.getId();

        CountDownLatch remove = countDownLatchMap.remove(id);
        if (remove != null) {
            confirmEventMap.put(id, channelPushConfirm);
            remove.countDown();
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

            ChannelPushConfirm channelPushConfirm = confirmEventMap.get(id);
            if (channelPushConfirm == null) {
                throw new GatewayException("ACK响应为空");
            }

            AckTypeEnum ackType = channelPushConfirm.getAckType();

            if (Objects.requireNonNull(ackType) == AckTypeEnum.ERROR) {
                throw new GatewayException(StrUtil.emptyToDefault(channelPushConfirm.getErrorMessage(), "ACK失败"));
            }
        } catch (InterruptedException interruptedException) {
            throw new GatewayException("等待ACK被中断");
        } finally {
            countDownLatchMap.remove(id);
            confirmEventMap.remove(id);
        }
    }

    /**
     * 写入offset
     *
     * @param offset
     * @param channelName
     */
    private void writeOffset(Long offset, String channelName, String appId) {
        FileUtil.writeString(offset.toString(), this.getOffsetFile(channelName, appId), StandardCharsets.UTF_8);
    }

    /**
     * 获取offset
     *
     * @param channelName
     */
    private Long getOffset(String channelName, String appId) {
        String filePath = this.getOffsetFile(channelName, appId);
        if (!FileUtil.exist(filePath)) {
            return 0L;
        }

        String s = FileUtil.readString(this.getOffsetFile(channelName, appId), StandardCharsets.UTF_8);
        return StrUtil.isEmpty(s) ? 0L : Convert.toLong(s, 0L);
    }

    /**
     * 获取offset文件
     *
     * @param channelName
     * @return
     */
    private String getOffsetFile(String channelName, String appId) {
        return configLoad.getOffsetFilePath() + channelName + File.separator + appId;
    }

    /**
     * 获取offset文件夹
     *
     * @param channelName
     * @return
     */
    private String getOffsetDir(String channelName) {
        return GlobalConstants.Client.DEFAULT_OFFSET_PATH + channelName;
    }

    @Data
    private static class ChannelSubInfo implements Serializable {

        private Listen listen;

        private ChannelSub channelSub;

    }
}
