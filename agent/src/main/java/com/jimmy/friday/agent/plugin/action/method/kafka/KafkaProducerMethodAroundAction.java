package com.jimmy.friday.agent.plugin.action.method.kafka;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.agent.support.RunTopologySupport;
import com.jimmy.friday.agent.support.TopologySupport;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.other.ConfigConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class KafkaProducerMethodAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        ProducerRecord record = (ProducerRecord) param[0];
        String topicName = record.topic();

        Context context = ContextHold.getContext();
        if (context != null) {
            record.headers().add(ConfigConstants.HEADER_TRACE_ID_KEY, context.getTraceId().getBytes(StandardCharsets.UTF_8));
            record.headers().add(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY, context.getIsNeedPushLog().toString().getBytes(StandardCharsets.UTF_8));
        }

        String invokeRemark = "/Topic:" + topicName;
        List<String> servers = (List<String>) enhancedInstance.getDynamicField().getDynamic();

        for (String server : servers) {
            String[] split = server.split(":");

            Topology kafka = new Topology();
            kafka.setMachine(split[0]);
            kafka.setApplication("kafka");
            kafka.setType("kafka");
            TopologySupport.getInstance().push(ConfigLoad.getDefault().getTopology(), kafka, invokeRemark, "kafka");

            if (context != null && context.getIsNeedPushLog()) {
                RunTopologySupport.getInstance().send(ConfigLoad.getDefault().getTopology(), kafka, invokeRemark, "kafka", context.getTraceId());
            }
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {

    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
