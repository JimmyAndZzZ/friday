package com.jimmy.friday.protocol.registered;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.protocol.connector.NettyConnector;
import com.jimmy.friday.protocol.exception.ProtocolException;
import com.jimmy.friday.protocol.annotations.RegisteredType;
import com.jimmy.friday.protocol.base.Input;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.base.Push;
import com.jimmy.friday.protocol.condition.WebSocketCondition;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import com.jimmy.friday.protocol.netty.NioWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@Slf4j
@RegisteredType(type = ProtocolEnum.WEBSOCKET, condition = WebSocketCondition.class)
public class WebSocketRegistered extends BaseRegistered {

    private Set<String> reconnection = Sets.newConcurrentHashSet();

    private Map<String, NettyConnector> connectorMap = Maps.newHashMap();

    private Map<String, WebSocketClient> webSocketClientMap = Maps.newHashMap();

    private Map<String, NioWebSocketHandler> nioWebSocketHandlerMap = Maps.newHashMap();

    @Override
    public Output outputGene(Protocol info) {
        String client = info.getTopic();
        Integer port = info.getPort();
        Boolean isWsServerToclient = info.getIsWsServerToClient();
        //判断是否需要从配置文件读取
        if (ReUtil.isMatch(REG_CONFIG, client)) {
            String group1 = ReUtil.getGroup1(REG_CONFIG, client);
            String property = environment.getProperty(group1);
            if (StrUtil.isEmpty(property)) {
                throw new ProtocolException(group1 + "property is empty");
            }

            client = property;
        }

        String finalClient = client;
        Output output;
        //判断是否为服务端往客户端推送
        if (!isWsServerToclient) {
            output = message -> {
                WebSocketClient webSocketClient = webSocketClientMap.get(finalClient);
                if (webSocketClient == null) {
                    synchronized (WebSocketRegistered.class) {
                        webSocketClient = webSocketClientMap.get(finalClient);
                        if (webSocketClient == null) {
                            try {
                                webSocketClient = new WebSocketClient(new URI(finalClient), new Draft_6455()) {
                                    @Override
                                    public void onOpen(ServerHandshake handshakedata) {
                                        log.info("[websocket] 连接成功");
                                    }

                                    @Override
                                    public void onMessage(String message) {

                                    }

                                    @Override
                                    public void onClose(int code, String reason, boolean remote) {
                                        log.info("[websocket] 退出连接");
                                    }

                                    @Override
                                    public void onError(Exception ex) {
                                        log.info("[websocket] 连接错误={}", ex.getMessage());
                                    }
                                };
                            } catch (Exception e) {
                                log.error("创建Websocket客户端失败", e);
                            }

                            try {
                                webSocketClient.connectBlocking();
                                webSocketClientMap.put(finalClient, webSocketClient);

                            } catch (InterruptedException e) {
                                throw new ProtocolException("websock连接失败");
                            }
                        }
                    }
                }

                try {
                    webSocketClient.send(message);
                } catch (WebsocketNotConnectedException we) {
                    if (reconnection.add(finalClient)) {
                        log.info("正在重连");
                        return null;
                    }

                    log.info("准备重连");
                    try {
                        webSocketClient.reconnectBlocking();
                        webSocketClient.send(message);
                    } catch (InterruptedException e) {
                        log.error("重连中断");
                    } finally {
                        reconnection.remove(finalClient);
                    }
                }

                return null;
            };
        } else {
            output = new Push() {
                @Override
                public Object send(String message) {
                    if (port.intValue() == 0) {
                        log.error("未设置端口号");
                        return null;
                    }

                    NioWebSocketHandler nioWebSocketHandler = nioWebSocketHandlerMap.get(finalClient + ":" + port);
                    nioWebSocketHandler.sendAllMessage(message);
                    return null;
                }

                @Override
                public Object send(String message, String userId) {
                    if (port.intValue() == 0) {
                        log.error("未设置端口号");
                        return null;
                    }

                    NioWebSocketHandler nioWebSocketHandler = nioWebSocketHandlerMap.get(finalClient + ":" + port);
                    nioWebSocketHandler.sendMessage(message, userId);
                    return null;
                }
            };
        }

        return output;
    }

    @Override
    public Input inputGene(Protocol info, Input input) {
        String server = info.getTopic();
        Integer port = info.getPort();

        if (port.intValue() == 0) {
            throw new IllegalArgumentException("websocket端口未设置");
        }

        NioWebSocketHandler nioWebSocketHandler = new NioWebSocketHandler(input);

        String s = server + ":" + port;
        NettyConnector nettyConnector = new NettyConnector(port, server, nioWebSocketHandler);
        nioWebSocketHandlerMap.put(s, nioWebSocketHandler);
        connectorMap.put(s, nettyConnector);

        new Thread(() -> {
            try {
                nettyConnector.start();
            } catch (Exception e) {
                log.error("websocket启动失败, server : {} ,  port : {}  , exception : {}", server, port, e);
            }
        }).start();

        return input;
    }

    @Override
    public void close(Protocol info) {
        String server = info.getTopic();
        Integer port = info.getPort();

        String s = server + ":" + port;

        NettyConnector nettyConnector = connectorMap.get(s);
        if (nettyConnector != null) {
            nettyConnector.close();
        }

        connectorMap.remove(s);
        nioWebSocketHandlerMap.remove(s);
    }
}
