package com.jimmy.friday.agent.plugin.action.method.kafka;

import com.google.common.base.Strings;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.agent.support.RunTopologySupport;
import com.jimmy.friday.agent.support.TopologySupport;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.other.ConfigConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class KafkaConsumerMethodAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {

    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        if (result != null) {
            Map<TopicPartition, List<ConsumerRecord<?, ?>>> records = (Map<TopicPartition, List<ConsumerRecord<?, ?>>>) (result);

            if (records.size() > 0) {
                EnhancedField dynamicField = enhancedInstance.getDynamicField();

                for (List<ConsumerRecord<?, ?>> consumerRecords : records.values()) {
                    for (ConsumerRecord<?, ?> record : consumerRecords) {
                        Headers headers = record.headers();

                        String traceId = this.getHeader(ConfigConstants.HEADER_TRACE_ID_KEY, headers);
                        if (!Strings.isNullOrEmpty(traceId)) {
                            Context context = new Context();
                            context.setTraceId(traceId);

                            String isNeedPushLog = this.getHeader(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY, headers);
                            if (!Strings.isNullOrEmpty(isNeedPushLog)) {
                                context.setIsNeedPushLog(Boolean.valueOf(isNeedPushLog));
                            }

                            ContextHold.setContext(context);
                        }

                        String invokeRemark = "/Topic:" + dynamicField.getAttachment("topics", String.class);
                        List<String> servers = (List<String>) enhancedInstance.getDynamicField().getDynamic();

                        String groupId = dynamicField.getAttachment("groupId", String.class);
                        if (!Strings.isNullOrEmpty(groupId)) {
                            invokeRemark = invokeRemark + "/GroupId:" + groupId;
                        }

                        for (String server : servers) {
                            String[] split = server.split(":");

                            Topology kafka = new Topology();
                            kafka.setMachine(split[0]);
                            kafka.setApplication("kafka");
                            kafka.setType("kafka");
                            TopologySupport.getInstance().push(kafka, ConfigLoad.getDefault().getTopology(), invokeRemark, "kafka");

                            if (!Strings.isNullOrEmpty(traceId) && ContextHold.getContext().getIsNeedPushLog()) {
                                RunTopologySupport.getInstance().send(kafka, ConfigLoad.getDefault().getTopology(), invokeRemark, "kafka", traceId);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[]
            parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }

    @Override
    public void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[]
            param) {
        //清除上下文
        ContextHold.removeContext();
    }

    /**
     * 获取头信息
     *
     * @param key
     * @param headers
     * @return
     */
    private String getHeader(String key, Headers headers) {
        Iterator<Header> traceIterator = headers.headers(key).iterator();
        if (traceIterator.hasNext()) {
            return new String(traceIterator.next().value(), StandardCharsets.UTF_8);
        }

        return null;
    }
}
