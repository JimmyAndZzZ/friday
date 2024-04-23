package com.jimmy.friday.framework.netty.client;

import cn.hutool.core.map.MapUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.ClientConnect;
import com.jimmy.friday.framework.base.Callback;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@ChannelHandler.Sharable
public class GatewayClientHandler extends SimpleChannelInboundHandler<Event> {

    private final ExecutorService executorService = new ThreadPoolExecutor(10, 30, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private final Map<EventTypeEnum, Process> processMap = new HashMap<>();

    private ConfigLoad configLoad;

    private GatewayClient gatewayClient;

    private ApplicationContext applicationContext;

    public GatewayClientHandler(ConfigLoad configLoad, ApplicationContext applicationContext, GatewayClient gatewayClient) {
        super();
        this.configLoad = configLoad;
        this.gatewayClient = gatewayClient;
        this.applicationContext = applicationContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //初始化本地处理类
        if (MapUtil.isEmpty(processMap)) {
            Map<String, Process> beansOfType = applicationContext.getBeansOfType(Process.class);
            beansOfType.values().forEach(bean -> processMap.put(bean.type(), bean));
        }
        //本地回调初始化
        Map<String, Callback> callbackMap = applicationContext.getBeansOfType(Callback.class);
        for (Callback value : callbackMap.values()) {
            value.prepare(ctx);
        }
        //客户端连接
        ClientConnect clientConnect = new ClientConnect();
        clientConnect.setId(configLoad.getId());
        ctx.writeAndFlush(new Event(EventTypeEnum.CLIENT_CONNECT, JsonUtil.toString(clientConnect)));

        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Event event) throws Exception {
        String type = event.getType();

        EventTypeEnum eventTypeEnum = EventTypeEnum.queryByCode(type);
        if (eventTypeEnum == null) {
            return;
        }

        try {
            executorService.submit(() -> {
                Process process = processMap.get(eventTypeEnum);
                if (process != null) {
                    process.process(event, ctx);
                }
            });
        } catch (RejectedExecutionException e) {
            log.error("Thread Pool Full");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        gatewayClient.connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
