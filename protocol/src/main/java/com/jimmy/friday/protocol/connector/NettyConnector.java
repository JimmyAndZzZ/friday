package com.jimmy.friday.protocol.connector;

import com.jimmy.friday.protocol.netty.NioWebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyConnector {

    private int port;

    private String server;

    private Channel serverChannel;

    private NioWebSocketHandler nioWebSocketHandler;

    public NettyConnector(int port, String server, NioWebSocketHandler nioWebSocketHandler) {
        this.port = port;
        this.server = server;
        this.nioWebSocketHandler = nioWebSocketHandler;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.option(ChannelOption.SO_BACKLOG, 1024);
            sb.group(group, bossGroup) // 绑定线程池
                    .channel(NioServerSocketChannel.class) // 指定使用的channel
                    .localAddress(this.port)// 绑定监听端口
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
                            ch.pipeline().addLast(new HttpServerCodec());
                            //以块的方式来写的处理器
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new HttpObjectAggregator(8192));
                            ch.pipeline().addLast(nioWebSocketHandler);
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler(server, "WebSocket", true, 65536 * 10));
                        }
                    });
            ChannelFuture cf = sb.bind().sync(); // 服务器异步创建绑定
            serverChannel = cf.channel();
            serverChannel.closeFuture().sync(); // 关闭服务器通道
        } finally {
            group.shutdownGracefully().sync(); // 释放线程池资源
            bossGroup.shutdownGracefully().sync();
        }
    }

    public void close() {
        if (serverChannel != null) {
            log.info("netty 关闭服务端");
            serverChannel.close();
            serverChannel = null;
        }
    }
}
