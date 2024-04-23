package com.jimmy.friday.client;

import com.jimmy.friday.boot.base.Listen;
import com.jimmy.friday.client.support.ChannelSupport;

public class Channel {

    private Channel() {


    }

    public static SubBuilder sub(String channelName) {
        SubBuilder subBuilder = new SubBuilder();
        subBuilder.channelName = channelName;
        return subBuilder;
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