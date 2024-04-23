package com.jimmy.friday.client.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.base.Listen;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.ChannelCancelSub;
import com.jimmy.friday.boot.message.gateway.ChannelReceive;
import com.jimmy.friday.boot.message.gateway.ChannelSub;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.client.netty.client.NettyConnector;
import com.jimmy.friday.client.netty.client.NettyConnectorPool;
import lombok.Data;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ChannelSupport {

    private final static Map<String, ChannelSubInfo> INFO_MAP = new ConcurrentHashMap<>();

    private final static ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static List<ChannelSub> getChannelSubList() {
        return MapUtil.isEmpty(INFO_MAP) ? new ArrayList<>() : INFO_MAP.values().stream().map(ChannelSubInfo::getChannelSub).collect(Collectors.toList());
    }

    public static void cancelSub(String name, String appId, String server) {
        if (StrUtil.isEmpty(appId)) {
            throw new GatewayException("appId为空");
        }

        if (StrUtil.isEmpty(name)) {
            throw new GatewayException("channel为空");
        }

        String key = appId + ":" + name;
        INFO_MAP.remove(key);

        FileUtil.del(getOffsetFile(name, appId));

        NettyConnector nettyConnector = NettyConnectorPool.get(server);

        ChannelCancelSub channelCancelSub = new ChannelCancelSub();
        channelCancelSub.setAppId(appId);
        channelCancelSub.setChannelName(name);
        nettyConnector.send(channelCancelSub);

        FileUtil.del(getOffsetFile(name, appId));
    }

    public static void sub(String name, String appId, Listen listen, String server) {
        if (StrUtil.isEmpty(appId)) {
            throw new GatewayException("appId为空");
        }

        if (StrUtil.isEmpty(name)) {
            throw new GatewayException("channel为空");
        }

        String key = appId + ":" + name;

        if (INFO_MAP.containsKey(key)) {
            throw new GatewayException("已订阅");
        }

        if (!FileUtil.exist(getOffsetDir(name))) {
            FileUtil.mkdir(getOffsetDir(name));
        }

        ChannelSub channelSub = new ChannelSub();
        channelSub.setAppId(appId);
        channelSub.setChannelName(name);
        channelSub.setApplicationId(NettyConnectorPool.getId());

        ChannelSubInfo channelSubInfo = new ChannelSubInfo();
        channelSubInfo.setListen(listen);
        channelSubInfo.setChannelSub(channelSub);

        ChannelSubInfo put = INFO_MAP.putIfAbsent(key, channelSubInfo);
        if (put != null) {
            throw new GatewayException("已订阅");
        }

        NettyConnector nettyConnector = NettyConnectorPool.get(server);
        nettyConnector.send(channelSub);
    }

    public static void receive(ChannelReceive channelReceive) {
        try {
            String appId = channelReceive.getAppId();
            Long offset = channelReceive.getOffset();
            String message = channelReceive.getMessage();
            String channelName = channelReceive.getChannelName();

            if (MapUtil.isEmpty(INFO_MAP)) {
                return;
            }

            Collection<ChannelSubInfo> values = INFO_MAP.values();

            for (ChannelSubInfo value : values) {
                Listen listen = value.getListen();
                String subAppId = value.getChannelSub().getAppId();
                String subChannelName = value.getChannelSub().getChannelName();

                if (!subChannelName.equals(channelName) || !subAppId.equals(appId)) {
                    continue;
                }

                Long lastOffset = getOffset(channelName, appId);
                if (lastOffset != null && lastOffset >= offset) {
                    continue;
                }

                writeOffset(offset, channelName, appId);
                EXECUTOR_SERVICE.submit(() -> listen.listen(message));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入offset
     *
     * @param offset
     * @param channelName
     */
    private static void writeOffset(Long offset, String channelName, String appId) {
        FileUtil.writeString(offset.toString(), getOffsetFile(channelName, appId), StandardCharsets.UTF_8);
    }

    /**
     * 获取offset
     *
     * @param channelName
     */
    private static Long getOffset(String channelName, String appId) {
        String filePath = getOffsetFile(channelName, appId);
        if (!FileUtil.exist(filePath)) {
            return 0L;
        }

        String s = FileUtil.readString(getOffsetFile(channelName, appId), StandardCharsets.UTF_8);
        return StrUtil.isEmpty(s) ? 0L : Convert.toLong(s, 0L);
    }

    /**
     * 获取offset文件
     *
     * @param channelName
     * @return
     */
    private static String getOffsetFile(String channelName, String appId) {
        return GlobalConstants.Client.DEFAULT_OFFSET_PATH + channelName + File.separator + appId;
    }

    /**
     * 获取offset文件夹
     *
     * @param channelName
     * @return
     */
    private static String getOffsetDir(String channelName) {
        return GlobalConstants.Client.DEFAULT_OFFSET_PATH + channelName;
    }

    @Data
    private static class ChannelSubInfo implements Serializable {

        private Listen listen;

        private ChannelSub channelSub;

    }
}
