package com.jimmy.friday.framework.process.gateway;

import cn.hutool.core.util.IdUtil;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.RpcProtocolInvoke;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.TransmitSupport;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcProtocolInvokeProcess implements Process<RpcProtocolInvoke> {

    private final Map<Long, CountDownLatch> confirm = new ConcurrentHashMap<>();

    private final Map<Long, GatewayResponse> response = new ConcurrentHashMap<>();

    private TransmitSupport transmitSupport;

    public RpcProtocolInvokeProcess(TransmitSupport transmitSupport) {
        this.transmitSupport = transmitSupport;
    }

    @Override
    public void process(RpcProtocolInvoke rpcProtocolInvoke, ChannelHandlerContext ctx) {
        Long traceId = rpcProtocolInvoke.getTraceId();

        CountDownLatch countDownLatch = this.confirm.get(traceId);
        if (countDownLatch != null) {
            this.confirm.remove(traceId);
            this.response.put(traceId, rpcProtocolInvoke.getGatewayResponse());
            countDownLatch.countDown();
        }
    }

    public GatewayResponse invoke(GatewayRequest gatewayRequest) {
        Long traceId = IdUtil.getSnowflake(1, 1).nextId();

        RpcProtocolInvoke rpcProtocolInvoke = new RpcProtocolInvoke();
        rpcProtocolInvoke.setTraceId(traceId);
        rpcProtocolInvoke.setGatewayRequest(gatewayRequest);
        try {
            //阻塞
            CountDownLatch countDownLatch = new CountDownLatch(1);
            confirm.put(traceId, countDownLatch);

            transmitSupport.send(rpcProtocolInvoke);

            countDownLatch.await(gatewayRequest.getTimeout() + GlobalConstants.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            //等待超时
            if (countDownLatch.getCount() != 0L) {
                confirm.remove(traceId);

                GatewayResponse gatewayResponse = new GatewayResponse();
                gatewayResponse.setIsSuccess(false);
                gatewayResponse.setExceptionClass(GatewayException.class.getName());
                gatewayResponse.setError("服务端未响应");
                return gatewayResponse;
            }

            GatewayResponse gatewayResponse = response.remove(traceId);
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

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.RPC_PROTOCOL_INVOKE;
    }
}
