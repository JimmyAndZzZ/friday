package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventTypeEnum {

    SERVICE_REGISTER("serviceRegister", "服务注册"),
    SERVICE_DESTROY("serviceDestroy", "服务下线"),
    SERVICE_RELOAD("serviceReload", "服务重载"),
    AGENT_REGISTER("agentRegister", "agent注册"),
    HEARTBEAT("heartbeat", "心跳检测"),
    GATEWAY_INVOKE("gatewayInvoke", "网关调用"),
    RPC_PROTOCOL_INVOKE("rpcProtocolInvoke", "RPC调用"),
    INVOKE_CALLBACK("invokeCallback", "调用回调"),
    AGENT_LOG("agentLog", "日志"),
    AGENT_COMMAND("agentCommand", "命令"),
    AGENT_TOPOLOGY("agentTopology", "拓扑"),
    AGENT_QPS("agentQps", "qps"),
    AGENT_POINT("agentPoint", "锚点"),
    AGENT_HEARTBEAT("agentHeartbeat", "心跳检测"),
    AGENT_RUN("agentRun", "运行拓扑"),
    AGENT_SHUTDOWN("agentShutdown", "agent关闭"),
    TRANSACTION_REGISTER("transactionRegister", "事务注册"),
    TRANSACTION_NOTIFY("transactionNotify", "事务通知"),
    TRANSACTION_SUBMIT("transactionSubmit", "事务提交"),
    TRANSACTION_SUBMIT_ACK("transactionSubmitAck", "事务提交确认"),
    TRANSACTION_ACK("transactionAck", "事务确认"),
    TRANSACTION_TIMEOUT("transactionTimeout", "事务超时"),
    TRANSACTION_COMPENSATION("transactionCompensation", "事务补偿"),
    TRANSACTION_REFUND("transactionRefund", "事务退回"),
    CLIENT_CONNECT("clientConnect", "成功连接"),
    CLIENT_DISCONNECT("clientDisconnect", "断开连接"),
    CHANNEL_PUSH("channelPush","频道推送"),
    CHANNEL_SUB("channelSub","频道订阅"),
    CHANNEL_CANCEL_SUB("channelCancelSub","频道取消订阅"),
    CHANNEL_RECEIVE("channelReceive","频道接收"),
    CHANNEL_ACK("channelAck","频道消息确认");

    private String code;

    private String message;

    public static EventTypeEnum queryByCode(String code) {
        for (EventTypeEnum value : EventTypeEnum.values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        return null;
    }
}
