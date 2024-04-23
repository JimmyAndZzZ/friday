package com.jimmy.friday.agent.plugin.action.construct;

import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.EnhancedField;

public class RunnableConstructInterceptorAction extends BaseConstructInterceptorAction {
    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] allArguments) throws Throwable {
        Context context = ContextHold.getContext();
        if (context != null) {
            EnhancedField enhancedField = new EnhancedField(context.getTraceId());
            enhancedField.setAttachment("isNeedPushLog", context.getIsNeedPushLog());
            enhancedInstance.setDynamicField(enhancedField);
        }
    }
}
