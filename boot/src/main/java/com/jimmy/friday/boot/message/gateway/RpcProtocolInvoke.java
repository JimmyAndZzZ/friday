package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class RpcProtocolInvoke implements Message {

    private Long traceId;

    private GatewayRequest gatewayRequest;

    private GatewayResponse gatewayResponse;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.RPC_PROTOCOL_INVOKE;
    }
}
