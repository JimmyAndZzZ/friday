package com.jimmy.friday.agent.plugin.action.method.rabbitmq;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Topology;
import com.rabbitmq.client.AMQP;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.agent.support.RunTopologySupport;
import com.jimmy.friday.agent.support.TopologySupport;
import com.jimmy.friday.boot.other.ConfigConstants;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class ChannelNPublishMethodAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        AMQP.BasicProperties properties = (AMQP.BasicProperties) param[4];
        AMQP.BasicProperties.Builder propertiesBuilder;
        Map<String, Object> headers = Maps.newHashMap();

        Context context = ContextHold.getContext();
        if (context != null) {
            headers.put(ConfigConstants.HEADER_TRACE_ID_KEY, context.getTraceId());
            headers.put(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY, context.getIsNeedPushLog());
        }

        String queueName = (String) param[1];
        String url = (String) enhancedInstance.getDynamicField().getDynamic();

        String invokeRemark = "/Queue:" + queueName;

        String[] split = url.split(",");
        for (String s : split) {
            Topology rabbitmq = new Topology();
            rabbitmq.setMachine(s);
            rabbitmq.setApplication("rabbitmq");
            rabbitmq.setType("rabbitmq");
            TopologySupport.getInstance().push(ConfigLoad.getDefault().getTopology(), rabbitmq, invokeRemark, "rabbitmq");

            if (context != null && context.getIsNeedPushLog()) {
                RunTopologySupport.getInstance().send(ConfigLoad.getDefault().getTopology(), rabbitmq, invokeRemark, "rabbitmq", context.getTraceId());
            }
        }

        if (properties != null) {
            propertiesBuilder = properties.builder()
                    .appId(properties.getAppId())
                    .clusterId(properties.getClusterId())
                    .contentEncoding(properties.getContentEncoding())
                    .contentType(properties.getContentType())
                    .correlationId(properties.getCorrelationId())
                    .deliveryMode(properties.getDeliveryMode())
                    .expiration(properties.getExpiration())
                    .messageId(properties.getMessageId())
                    .priority(properties.getPriority())
                    .replyTo(properties.getReplyTo())
                    .timestamp(properties.getTimestamp())
                    .type(properties.getType())
                    .userId(properties.getUserId());
            // copy origin headers
            if (properties.getHeaders() != null) {
                headers.putAll(properties.getHeaders());
            }
        } else {
            propertiesBuilder = new AMQP.BasicProperties.Builder();
        }

        param[4] = propertiesBuilder.headers(headers).build();
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
