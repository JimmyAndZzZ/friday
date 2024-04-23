package com.jimmy.friday.center.netty;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.center.base.Close;
import com.jimmy.friday.center.config.GatewayConfigProperties;
import com.jimmy.friday.center.netty.codec.NettyDecoder;
import com.jimmy.friday.center.netty.codec.NettyEncoder;
import com.jimmy.friday.center.support.ActionSupport;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CenterSever implements Close {

    private final ExecutorService executorService = new ThreadPoolExecutor(10, 120, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Autowired
    private ActionSupport actionSupport;

    @Autowired
    private GatewayConfigProperties gatewayConfigProperties;

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            bootstrap.group(boss, worker)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                    .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(4096));
                            pipeline.addLast("decoder", new NettyDecoder(Event.class));
                            pipeline.addLast("encoder", new NettyEncoder(Event.class));
                            pipeline.addLast(new CenterEventHandler(actionSupport, executorService));

                        }
                    });
            ChannelFuture f = bootstrap.bind(gatewayConfigProperties.getServerPort()).sync();

            log.info("gateway server start port:{}", gatewayConfigProperties.getServerPort());

            f.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("服务端启动失败", e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    @Override
    public void close() {
        ChannelHandlerPool.close();
        executorService.shutdown();
    }
}
