package com.jimmy.friday.client;

import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.base.Listen;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.client.support.ChannelSupport;

public class Channel {

    private Channel() {


    }

    public static SubBuilder sub(String channelName) {
        SubBuilder subBuilder = new SubBuilder();
        subBuilder.channelName = channelName;
        return subBuilder;
    }

    public static StreamBuilder stream() {
        return new StreamBuilder();
    }

    public static class StreamBuilder {

        private String tableName;

        private String database;

        private String appId;

        private String server;

        public StreamBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public StreamBuilder setServer(String server) {
            this.server = server;
            return this;
        }

        public StreamBuilder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public StreamBuilder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public void listen(Listen listen) {
            if (StrUtil.isEmpty(database)) {
                throw new GatewayException("数据源名未定义");
            }

            if (StrUtil.isEmpty(tableName)) {
                throw new GatewayException("表名未定义");
            }

            ChannelSupport.sub(database + StrUtil.UNDERLINE + tableName, appId, listen, server);
        }

        public void cancel() {
            if (StrUtil.isEmpty(database)) {
                throw new GatewayException("数据源名未定义");
            }

            if (StrUtil.isEmpty(tableName)) {
                throw new GatewayException("表名未定义");
            }

            ChannelSupport.cancelSub(database + StrUtil.UNDERLINE + tableName, appId, server);
        }
    }

    public static class SubBuilder {

        private String channelName;

        private String appId;

        private String server;

        public SubBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public SubBuilder setServer(String server) {
            this.server = server;
            return this;
        }

        public void listen(Listen listen) {
            ChannelSupport.sub(channelName, appId, listen, server);
        }

        public void cancel() {
            ChannelSupport.cancelSub(channelName, appId, server);
        }
    }
}