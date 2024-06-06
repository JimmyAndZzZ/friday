package com.jimmy.friday.framework;

import cn.hutool.core.util.IdUtil;
import com.jimmy.friday.boot.base.Listen;
import com.jimmy.friday.framework.support.ChannelSupport;

public class Channel {

    private Channel() {


    }

    public static PushBuilder push() {
        return new PushBuilder();
    }

    public static SubBuilder sub(String channelName) {
        SubBuilder subBuilder = new SubBuilder();
        subBuilder.channelName = channelName;
        return subBuilder;
    }

    public static class PushBuilder {

        private String message;

        private String channelName;

        public PushBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public PushBuilder setChannelName(String channelName) {
            this.channelName = channelName;
            return this;
        }

        public void finish() {
            Boot.getApplicationContext().getBean(ChannelSupport.class).push(IdUtil.getSnowflake(1, 1).nextId(), message);
        }
    }

    public static class SubBuilder {

        private String channelName;

        private String appId;

        public SubBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public void listen(Listen listen) {
            Boot.getApplicationContext().getBean(ChannelSupport.class).sub(channelName, appId, listen);
        }

        public void cancel() {
            Boot.getApplicationContext().getBean(ChannelSupport.class).cancelSub(channelName, appId);
        }
    }
}