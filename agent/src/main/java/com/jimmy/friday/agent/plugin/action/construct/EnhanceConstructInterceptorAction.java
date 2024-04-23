package com.jimmy.friday.agent.plugin.action.construct;

import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.support.TraceSupport;
import com.jimmy.friday.agent.utils.JsonUtil;

import java.util.Date;

public class EnhanceConstructInterceptorAction extends BaseConstructInterceptorAction {
    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] param) throws Throwable {
        Context context = ContextHold.getContext();
        if (context != null) {
            TraceSupport.getDefault().send(Trace.builder()
                    .date(new Date())
                    .level("INFO")
                    .logMessage("构造方法")
                    .methodName("Construct")
                    .className(this.getClassName())
                    .param(param != null && param.length > 0 ? JsonUtil.toString(param) : null)
                    .spanId(context.getSpanId())
                    .traceId(context.getTraceId())
                    .isLog(false).build());
        }
    }
}
