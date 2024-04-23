package com.jimmy.friday.client.netty.client;

import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.exception.ConnectionException;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.client.netty.codec.NettyDecoder;
import com.jimmy.friday.client.netty.codec.NettyEncoder;
import com.jimmy.friday.client.utils.JsonUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient {

    private String id;

    private String server;

    private Integer port;

    private Channel channel;

    private Bootstrap bootstrap;

    private EventLoopGroup group;

    @Getter
    private Boolean connectSuccess = false;

    private AtomicInteger retry = new AtomicInteger(0);

    public NettyClient(String server, String id) {
        List<String> split = StrUtil.split(server, ":");
        if (split.size() != 2) {
            throw new GatewayException("配置服务端地址异常");
        }

        this.id = id;
        this.server = split.get(0);
        this.port = Integer.valueOf(split.get(1));
    }

    public void send(Message message) {
        if (!connectSuccess) {
            throw new ConnectionException();
        }

        this.channel.writeAndFlush(new Event(message.type(), JsonUtil.toString(message)));
    }

    public void init() {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decoder", new NettyDecoder(Event.class));
                pipeline.addLast("encoder", new NettyEncoder(Event.class));
                pipeline.addLast(new NettyClientHandler(NettyClient.this, id));
            }
        });

        connect();
    }

    public void connect() {
        try {
            this.connectSuccess = false;

            ChannelFuture cf = bootstrap.connect(server, port);
            cf.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    System.out.println("客户端正在重连-----------");
                    //重连交给后端线程执行
                    if (retry.getAndIncrement() <= 15) {
                        // 重连交给后端线程执行
                        future.channel().eventLoop().schedule(this::connect, 5, TimeUnit.SECONDS);
                    }
                } else {
                    retry.set(0);
                    connectSuccess = true;
                }
            });
            //对通道关闭进行监听
            this.channel = cf.sync().channel();
        } catch (InterruptedException exception) {
            throw new GatewayException("客户端被中断");
        }
    }
}
