package com.jimmy.friday.agent.plugin.action.method.http;

import cn.hutool.http.HttpRequest;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.agent.support.TopologySupport;
import com.jimmy.friday.agent.utils.JsonUtil;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.other.ConfigConstants;

import java.lang.reflect.Method;

public class HutoolHttpExecuteAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        HttpRequest request = (HttpRequest) enhancedInstance;
        String url = request.getUrl();

        if (ConfigLoad.getDefault().httpUrlIsMatch(url)) {
            Context context = ContextHold.getContext();
            if (context != null) {
                request.header(ConfigConstants.HEADER_TRACE_ID_KEY, context.getTraceId());
                request.header(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY, context.getIsNeedPushLog().toString());
            }

            request.header(ConfigConstants.HEADER_PRODUCER_KEY, JsonUtil.toString(ConfigLoad.getDefault().getTopology()));
            TopologySupport.getInstance().push(ConfigLoad.getDefault().getTopology(), null, url, "http");
        }
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {

    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }
}
