package com.jimmy.friday.agent.plugin.action.method.rabbitmq;

import com.jimmy.friday.boot.core.agent.ContextHold;
import com.rabbitmq.client.Consumer;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.plugin.action.ext.RabbitMQConsumerExt;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;

import java.lang.reflect.Method;

public class ChannelNConsumerMethodAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        Consumer consumer = (Consumer) param[6];
        param[6] = new RabbitMQConsumerExt(consumer, (String) enhancedInstance.getDynamicField().getDynamic());
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {

    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }

    @Override
    public void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        //清除上下文
        ContextHold.removeContext();
    }
}
