package com.jimmy.friday.agent.plugin.action.construct.rabbitmq;

import com.rabbitmq.client.Connection;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.plugin.action.construct.BaseConstructInterceptorAction;

public class ChannelNConstructInterceptorAction extends BaseConstructInterceptorAction {

    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] param) throws Throwable {
        Connection connection = (Connection) param[0];
        String url = connection.getAddress().toString().replace("/", "");
        enhancedInstance.setDynamicField(new EnhancedField(url));
    }
}
