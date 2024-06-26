package com.jimmy.friday.boot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventTypeEnum {

    ACK("ack", "消息确认", false),
    SERVICE_REGISTER("serviceRegister", "服务注册", false),
    SERVICE_DESTROY("serviceDestroy", "服务下线", false),
    SERVICE_RELOAD("serviceReload", "服务重载", false),
    AGENT_REGISTER("agentRegister", "agent注册", false),
    GATEWAY_HEARTBEAT("heartbeat", "心跳检测", false),
    GATEWAY_INVOKE("gatewayInvoke", "网关调用", false),
    RPC_PROTOCOL_INVOKE("rpcProtocolInvoke", "RPC调用", false),
    INVOKE_CALLBACK("invokeCallback", "调用回调", false),
    AGENT_LOG("agentLog", "日志", false),
    AGENT_COMMAND("agentCommand", "命令", false),
    AGENT_TOPOLOGY("agentTopology", "拓扑", false),
    AGENT_QPS("agentQps", "qps", false),
    AGENT_POINT("agentPoint", "锚点", false),
    AGENT_HEARTBEAT("agentHeartbeat", "心跳检测", false),
    AGENT_RUN("agentRun", "运行拓扑", false),
    AGENT_SHUTDOWN("agentShutdown", "agent关闭", false),
    TRANSACTION_REGISTER("transactionRegister", "事务注册", true),
    TRANSACTION_NOTIFY("transactionNotify", "事务通知", true),
    TRANSACTION_SUBMIT("transactionSubmit", "事务提交", true),
    TRANSACTION_SUBMIT_ACK("transactionSubmitAck", "事务提交确认", false),
    TRANSACTION_CONFIRM("transactionConfirm", "事务确认", false),
    TRANSACTION_TIMEOUT("transactionTimeout", "事务超时", false),
    TRANSACTION_COMPENSATION("transactionCompensation", "事务补偿", false),
    TRANSACTION_REFUND("transactionRefund", "事务退回", false),
    CLIENT_CONNECT("clientConnect", "成功连接", false),
    CLIENT_DISCONNECT("clientDisconnect", "断开连接", false),
    CHANNEL_PUSH("channelPush", "频道推送", false),
    CHANNEL_SUB("channelSub", "频道订阅", false),
    CHANNEL_CANCEL_SUB("channelCancelSub", "频道取消订阅", false),
    CHANNEL_RECEIVE("channelReceive", "频道接收", false),
    CHANNEL_PUSH_CONFIRM("channelPushConfirm", "频道消息确认", false),
    SCHEDULE_REGISTER("scheduleRegister", "调度注册", false),
    SCHEDULE_INVOKE("scheduleInvoke", "调度运行", false),
    SCHEDULE_APPEND("scheduleAppend", "调度增加", true),
    SCHEDULE_DELETE("scheduleDelete", "调度删除", true),
    SCHEDULE_RESULT("scheduleResult", "调度结果", false),
    SCHEDULE_HEARTBEAT("scheduleHeartbeat", "调度心跳", false),
    SCHEDULE_INTERRUPT("scheduleInterrupt", "调度中断", false);

    private String code;

    private String message;

    private Boolean isNeedAck;

    public static EventTypeEnum queryByCode(String code) {
        for (EventTypeEnum value : EventTypeEnum.values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        return null;
    }
}
