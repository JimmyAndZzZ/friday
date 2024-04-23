package com.jimmy.friday.protocol.netty;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.protocol.base.Input;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@ChannelHandler.Sharable
public class NioWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private Input input;

    public NioWebSocketHandler(Input input) {
        this.input = input;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //添加到channelGroup通道组
        ChannelHandlerPool.putChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //添加到channelGroup 通道组
        ChannelHandlerPool.closeChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //首次连接是FullHttpRequest，处理参数
            if (null != msg && msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                String uri = request.uri();
                //提取参数
                Map<String, String> urlParams = getUrlParams(uri);
                if (urlParams.containsKey("userId")) {
                    ChannelHandlerPool.putSession(urlParams.get("userId"), ctx.channel().id());
                }
                //如果url包含参数，需要处理
                if (uri.contains("?")) {
                    String newUri = uri.substring(0, uri.indexOf("?"));
                    request.setUri(newUri);
                }
            } else if (msg instanceof TextWebSocketFrame) {
                //正常的TEXT消息类型
                TextWebSocketFrame frame = (TextWebSocketFrame) msg;
                String text = frame.text();
                input.invoke(text);
            }

        } catch (Exception e) {
            log.error("websocket消息消费失败", e);
        }

        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {

    }


    public void sendMessage(String message, String userId) {
        Channel channel = ChannelHandlerPool.getChannel(userId);
        if (channel != null && channel.isOpen()) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    public void sendAllMessage(String message) {
        List<Channel> channels = ChannelHandlerPool.allChannel();
        if (CollUtil.isNotEmpty(channels)) {
            channels.stream().forEach(channel -> {
                if (channel.isOpen()) {
                    channel.writeAndFlush(new TextWebSocketFrame(message));
                }
            });
        }
    }

    private static Map<String, String> getUrlParams(String url) {
        Map<String, String> map = Maps.newHashMap();
        url = url.replace("?", ";");
        if (!url.contains(";")) {
            return map;
        }
        if (url.split(";").length > 0) {
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr) {
                String key = s.split("=")[0];
                String value = s.split("=")[1];
                map.put(key, value);
            }
        }

        return map;
    }
}
