package com.jimmy.friday.agent.plugin.action.method.spring.rabbitmq;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.other.ConfigConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.lang.reflect.Method;

@Slf4j
public class SpringRabbitMQConsumerMethodAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        Message message = (Message) param[1];

        MessageProperties messageProperties = message.getMessageProperties();
        if (messageProperties != null) {
            Object o = messageProperties.getHeaders().get(ConfigConstants.HEADER_TRACE_ID_KEY);
            if (o != null) {
                Context context = new Context();
                context.setTraceId(o.toString());

                Object logNeedPush = messageProperties.getHeaders().get(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY);
                if (logNeedPush != null) {
                    context.setIsNeedPushLog(Boolean.valueOf(logNeedPush.toString()));
                }

                ContextHold.setContext(context);
            }
        }
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
