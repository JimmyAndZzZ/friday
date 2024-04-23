package com.jimmy.friday.agent.plugin.action.construct.kafka;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;
import com.jimmy.friday.agent.plugin.action.construct.BaseConstructInterceptorAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConstructorWithMapInterceptAction extends BaseConstructInterceptorAction {

    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] allArguments) throws Throwable {
        Map<String, ?> configArgument = (Map<String, ?>) allArguments[0];

        if (configArgument != null) {
            EnhancedField enhancedField = new EnhancedField();
            enhancedField.setDynamic(configArgument.get("bootstrap.servers"));
            enhancedField.setAttachment("groupId", configArgument.get("group.id"));
            enhancedInstance.setDynamicField(enhancedField);
        }
    }

    private List<String> convertToList(Object value) {
        if (value instanceof List)
            return (List<String>) value;
        else if (value instanceof String) {
            return Arrays.stream(((String) value).split(",")).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }


}
