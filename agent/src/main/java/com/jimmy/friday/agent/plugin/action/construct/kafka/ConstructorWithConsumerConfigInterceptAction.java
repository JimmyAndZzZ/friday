package com.jimmy.friday.agent.plugin.action.construct.kafka;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.plugin.action.construct.BaseConstructInterceptorAction;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class ConstructorWithConsumerConfigInterceptAction extends BaseConstructInterceptorAction {

    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] allArguments) throws Throwable {
        ConsumerConfig consumerConfig = (ConsumerConfig) allArguments[0];

        if (consumerConfig != null) {
            EnhancedField enhancedField = new EnhancedField();
            enhancedField.setDynamic(consumerConfig.getList("bootstrap.servers"));
            enhancedField.setAttachment("groupId", consumerConfig.getString("group.id"));
            enhancedInstance.setDynamicField(enhancedField);
        }
    }
}
