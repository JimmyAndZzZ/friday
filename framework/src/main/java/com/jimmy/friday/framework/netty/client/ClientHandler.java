package com.jimmy.friday.framework.netty.client;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.Ack;
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Event> {

    private final Map<EventTypeEnum, Class<?>> classMap = new HashMap<>();

    private final Map<EventTypeEnum, Process<?>> processMap = new HashMap<>();

    private final ExecutorService executorService = new ThreadPoolExecutor(10, 60,
            60L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    private Client client;

    private ConfigLoad configLoad;

    private ApplicationContext applicationContext;

    public ClientHandler(ConfigLoad configLoad, ApplicationContext applicationContext, Client client) throws Exception {
        super();
        this.configLoad = configLoad;
        this.client = client;
        this.applicationContext = applicationContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //初始化本地处理类
        if (MapUtil.isEmpty(processMap)) {
            Map<String, Process> beansOfType = applicationContext.getBeansOfType(Process.class);
            for (Process value : beansOfType.values()) {
                EventTypeEnum type = value.type();
                processMap.put(type, value);

                Type[] genericInterfaces = value.getClass().getGenericInterfaces();
                if (ArrayUtil.isNotEmpty(genericInterfaces)) {
                    Type genericInterface = genericInterfaces[0];
                    // 如果gType类型是ParameterizedType对象
                    if (genericInterface instanceof ParameterizedType) {
                        // 强制类型转换
                        ParameterizedType pType = (ParameterizedType) genericInterface;
                        // 取得泛型类型的泛型参数
                        Type[] tArgs = pType.getActualTypeArguments();
                        if (ArrayUtil.isNotEmpty(tArgs)) {
                            classMap.put(type, Class.forName(tArgs[0].getTypeName()));
                        }
                    }
                }
            }
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
        String message = event.getMessage();

        EventTypeEnum eventTypeEnum = EventTypeEnum.queryByCode(type);
        if (eventTypeEnum == null) {
            return;
        }

        try {
            executorService.execute(() -> {
                if (eventTypeEnum.getIsNeedAck()) {
                    Ack ack = new Ack();
                    ack.setId(event.getId());
                    ctx.writeAndFlush(new Event(EventTypeEnum.ACK, JsonUtil.toString(ack)));
                }

                Class<?> clazz = classMap.get(eventTypeEnum);
                Process process = processMap.get(eventTypeEnum);

                if (process == null || clazz == null) {
                    return;
                }

                process.process(JsonUtil.parseObject(message, clazz), ctx);
            });
        } catch (RejectedExecutionException e) {
            log.error("Thread Pool Full");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("事件处理异常", cause);
    }
}
