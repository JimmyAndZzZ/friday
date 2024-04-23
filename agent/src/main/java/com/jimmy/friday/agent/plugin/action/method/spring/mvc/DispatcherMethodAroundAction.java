package com.jimmy.friday.agent.plugin.action.method.spring.mvc;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

public class DispatcherMethodAroundAction extends BaseMethodsAroundAction {

    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = requestAttributes.getRequest();
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        String requestURI = request.getRequestURI();
        int serverPort = request.getServerPort();
        String queryString = request.getQueryString();

        String traceId = request.getHeader(ConfigConstants.HEADER_TRACE_ID_KEY);
        if (!Strings.isNullOrEmpty(traceId)) {
            Context context = new Context();
            context.setTraceId(traceId);

            String logNeedPush = request.getHeader(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY);
            if (logNeedPush != null) {
                context.setIsNeedPushLog(Boolean.valueOf(logNeedPush));
            }

            ContextHold.setContext(context);
        }

        String s = request.getHeader(ConfigConstants.HEADER_PRODUCER_KEY);
        if (!Strings.isNullOrEmpty(s)) {
            Topology up = JsonUtil.toBean(s, Topology.class);
            if (up != null) {
                String invokeRemark = scheme + "://" + serverName + ":" + serverPort + requestURI;
                // 判断是否有 GET 参数，并将其拼接到完整的 URL 中
                if (queryString != null && !queryString.isEmpty()) {
                    invokeRemark += "?" + queryString;
                }

                TopologySupport.getInstance().push(up, ConfigLoad.getDefault().getTopology(), invokeRemark, "http");

                if (!Strings.isNullOrEmpty(traceId) && ContextHold.getContext().getIsNeedPushLog()) {
                    RunTopologySupport.getInstance().send(up, ConfigLoad.getDefault().getTopology(), invokeRemark, "http", traceId);
                }
            }
        }

        ContextHold.setQpsFlagHolder(false);
    }

    @Override
    public void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost) {
        if (ContextHold.getQpsFlagHolder()) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            HttpServletRequest request = requestAttributes.getRequest();

            String requestURI = request.getRequestURI();
            String ipAddress = this.getIpAddress(request);

            if (!Strings.isNullOrEmpty(ipAddress)) {
                QpsSupport.getDefault().send(requestURI, new Date(), ipAddress, "http");
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

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
