package com.jimmy.friday.client.support;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.RpcProtocolInvoke;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.client.netty.client.NettyConnector;
import com.jimmy.friday.client.netty.client.NettyConnectorPool;
import com.jimmy.friday.client.utils.JsonUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GatewayInvokeSupport {

    private static final Map<Long, CountDownLatch> CONFIRM = new ConcurrentHashMap<>();

    private static final Map<Long, GatewayResponse> RESPONSE = new ConcurrentHashMap<>();

    public static void notify(Event event) {
        String message = event.getMessage();
        RpcProtocolInvoke rpcProtocolInvoke = JsonUtil.parseObject(message, RpcProtocolInvoke.class);

        Long traceId = rpcProtocolInvoke.getTraceId();

        CountDownLatch countDownLatch = CONFIRM.get(traceId);
        if (countDownLatch != null) {
            CONFIRM.remove(traceId);
            RESPONSE.put(traceId, rpcProtocolInvoke.getGatewayResponse());
            countDownLatch.countDown();
        }
    }

    public static GatewayResponse invoke(String server, GatewayRequest gatewayRequest) {
        if (StrUtil.isEmpty(server)) {
            throw new GatewayException("网关地址为空");
        }

        NettyConnector nettyConnector = NettyConnectorPool.get(server);

        Long traceId = IdUtil.getSnowflake(1, 1).nextId();

        RpcProtocolInvoke rpcProtocolInvoke = new RpcProtocolInvoke();
        rpcProtocolInvoke.setTraceId(traceId);
        rpcProtocolInvoke.setGatewayRequest(gatewayRequest);
        try {
            //阻塞
            CountDownLatch countDownLatch = new CountDownLatch(1);
            CONFIRM.put(traceId, countDownLatch);

            nettyConnector.send(rpcProtocolInvoke);

            countDownLatch.await(gatewayRequest.getTimeout() + GlobalConstants.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            //等待超时
            if (countDownLatch.getCount() != 0L) {
                CONFIRM.remove(traceId);

                GatewayResponse gatewayResponse = new GatewayResponse();
                gatewayResponse.setIsSuccess(false);
                gatewayResponse.setExceptionClass(GatewayException.class.getName());
                gatewayResponse.setError("服务端未响应");
                return gatewayResponse;
            }

            GatewayResponse gatewayResponse = RESPONSE.remove(traceId);
            if (gatewayResponse == null) {
                gatewayResponse = new GatewayResponse();
                gatewayResponse.setIsSuccess(false);
                gatewayResponse.setExceptionClass(GatewayException.class.getName());
                gatewayResponse.setError("服务端未响应");
                return gatewayResponse;
            }

            return gatewayResponse;
        } catch (InterruptedException e) {
            GatewayResponse gatewayResponse = new GatewayResponse();
            gatewayResponse.setIsSuccess(false);
            gatewayResponse.setExceptionClass(InterruptedException.class.getName());
            gatewayResponse.setError("调用中断");
            return gatewayResponse;
        }
    }
}
