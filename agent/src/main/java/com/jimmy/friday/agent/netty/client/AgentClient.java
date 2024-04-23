package com.jimmy.friday.agent.netty.client;

import com.jimmy.friday.agent.exception.AgentException;
import com.jimmy.friday.agent.netty.codec.NettyDecoder;
import com.jimmy.friday.agent.netty.codec.NettyEncoder;
import com.jimmy.friday.agent.utils.JsonUtil;
import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.exception.GatewayException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentClient {

    private String server;

    private Integer port;

    private Channel channel;

    private Bootstrap bootstrap;

    private EventLoopGroup group;

    private ExecutorService threadPoolExecutor;

    private AtomicInteger retry = new AtomicInteger(0);

    @Getter
    private Boolean connectSuccess = false;

    public AgentClient(String server) {
        this.threadPoolExecutor = Executors.newSingleThreadExecutor();

        String[] split = server.split(":");
        if (split.length != 2) {
            throw new GatewayException("配置服务端地址异常");
        }

        this.server = split[0];
        this.port = Integer.valueOf(split[1]);
        this.init();
    }

    public void send(Message message) {
        if (!connectSuccess) {
            return;
        }

        threadPoolExecutor.submit(() -> {
            this.channel.writeAndFlush(new Event(message.type(), JsonUtil.toString(message)));
        });
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
                pipeline.addLast(new AgentClientHandler(AgentClient.this));
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
            throw new AgentException("客户端被中断");
        }
    }
}
