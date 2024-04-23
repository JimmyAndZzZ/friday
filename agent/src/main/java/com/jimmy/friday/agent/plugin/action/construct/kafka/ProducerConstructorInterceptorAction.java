package com.jimmy.friday.agent.plugin.action.construct.kafka;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.plugin.action.construct.BaseConstructInterceptorAction;
import org.apache.kafka.clients.producer.ProducerConfig;

public class ProducerConstructorInterceptorAction extends BaseConstructInterceptorAction {

    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] allArguments) throws Throwable {
        ProducerConfig config = (ProducerConfig) allArguments[0];
        enhancedInstance.setDynamicField(new EnhancedField(config.getList("bootstrap.servers")));
    }
}
