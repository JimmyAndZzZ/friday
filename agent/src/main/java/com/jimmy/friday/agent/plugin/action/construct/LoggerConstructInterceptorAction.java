package com.jimmy.friday.agent.plugin.action.construct;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.support.TraceSupport;

import java.util.Date;

public class LoggerConstructInterceptorAction extends BaseConstructInterceptorAction {

    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] allArguments) throws Throwable {
        Context context = ContextHold.getContext();
        if (context != null) {
            if (enhancedInstance instanceof LoggingEvent) {
                LoggingEvent event = (LoggingEvent) enhancedInstance;

                Level level = event.getLevel();
                if (context.getIsNeedPushLog() && ConfigLoad.getDefault().logLevelIsMatch(level.levelStr)) {
                    TraceSupport.getDefault().send(Trace.builder()
                            .date(new Date())
                            .level(level.levelStr)
                            .logMessage(event.getFormattedMessage())
                            .className(this.getClassName())
                            .methodName("Construct")
                            .spanId(context.getSpanId())
                            .traceId(context.getTraceId())
                            .isLog(true).build());
                }
            }
        }
    }
}