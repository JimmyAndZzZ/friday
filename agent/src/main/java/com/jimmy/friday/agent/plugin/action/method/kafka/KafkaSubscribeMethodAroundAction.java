package com.jimmy.friday.agent.plugin.action.method.kafka;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.agent.utils.StringUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class KafkaSubscribeMethodAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        EnhancedField dynamicField = enhancedInstance.getDynamicField();
        if (parameterTypes[0] == Pattern.class) {
            List<String> list = Collections.singletonList(((Pattern) param[0]).pattern());
            dynamicField.setAttachment("topics", StringUtil.join(';', list.toArray(new String[0])));
        } else {
            Collection<String> topics = (Collection<String>) param[0];
            dynamicField.setAttachment("topics", StringUtil.join(';', topics.toArray(new String[0])));
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {

    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
