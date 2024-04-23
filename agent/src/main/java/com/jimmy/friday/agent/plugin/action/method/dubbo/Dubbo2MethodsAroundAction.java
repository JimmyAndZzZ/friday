package com.jimmy.friday.agent.plugin.action.method.dubbo;

import com.google.common.base.Strings;
import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.plugin.action.method.BaseMethodsAroundAction;
import com.jimmy.friday.agent.support.QpsSupport;
import com.jimmy.friday.agent.support.RunTopologySupport;
import com.jimmy.friday.agent.support.TopologySupport;
import com.jimmy.friday.agent.utils.JsonUtil;
import com.jimmy.friday.boot.core.agent.Context;
import com.jimmy.friday.boot.core.agent.ContextHold;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.other.ConfigConstants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.RpcContext;

import java.lang.reflect.Method;
import java.util.Date;


public class Dubbo2MethodsAroundAction extends BaseMethodsAroundAction {

    private static final String OPERATE_NAME_PREFIX = "Dubbo/";

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        RpcContext rpcContext = RpcContext.getContext();

        boolean isConsumer = rpcContext.isConsumerSide();

        if (isConsumer) {
            String serviceKey = rpcContext.getServiceKey();

            Context context = ContextHold.getContext();
            if (context != null) {
                rpcContext.setAttachment(ConfigConstants.HEADER_TRACE_ID_KEY, context.getTraceId());
                rpcContext.setAttachment(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY, context.getIsNeedPushLog().toString());
            }

            rpcContext.setAttachment(ConfigConstants.HEADER_PRODUCER_KEY, JsonUtil.toString(ConfigLoad.getDefault().getTopology()));
        } else {
            URL url = rpcContext.getUrl();
            String serviceKey = url.getServiceKey();
            String invokeRemark = OPERATE_NAME_PREFIX + serviceKey;

            String traceId = rpcContext.getAttachment(ConfigConstants.HEADER_TRACE_ID_KEY);
            if (!Strings.isNullOrEmpty(traceId)) {
                Context context = new Context();
                context.setTraceId(traceId);

                String logNeedPush = rpcContext.getAttachment(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY);
                if (!Strings.isNullOrEmpty(logNeedPush)) {
                    context.setIsNeedPushLog(Boolean.valueOf(logNeedPush));
                }

                ContextHold.setContext(context);
            }

            String producer = rpcContext.getAttachment(ConfigConstants.HEADER_PRODUCER_KEY);

            if (!Strings.isNullOrEmpty(producer)) {
                Topology up = JsonUtil.toBean(producer, Topology.class);
                if (up != null) {
                    TopologySupport.getInstance().push(up, ConfigLoad.getDefault().getTopology(), invokeRemark, "dubbo");

                    if (!Strings.isNullOrEmpty(traceId) && ContextHold.getContext().getIsNeedPushLog()) {
                        RunTopologySupport.getInstance().send(up, ConfigLoad.getDefault().getTopology(), invokeRemark, "dubbo", traceId);
                    }
                }
            }
        }

        ContextHold.setQpsFlagHolder(false);
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        if (ContextHold.getQpsFlagHolder()) {
            RpcContext rpcContext = RpcContext.getContext();

            boolean isConsumer = rpcContext.isConsumerSide();
            String serviceKey = rpcContext.getServiceKey();
            String remoteAddressString = rpcContext.getRemoteAddressString();
            String remoteApplicationName = rpcContext.getRemoteApplicationName();

            if (!isConsumer) {
                if (!Strings.isNullOrEmpty(remoteAddressString) && !Strings.isNullOrEmpty(remoteApplicationName)) {
                    QpsSupport.getDefault().send(serviceKey, new Date(), "applicationName:" + remoteApplicationName + ",ipAddress:" + remoteAddressString, "Dubbo");
                }
            }
        }
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost) {

    }

    @Override
    public void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        ContextHold.removeQpsFlagHolder();
    }
}
