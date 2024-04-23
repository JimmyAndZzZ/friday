package com.jimmy.friday.framework.other;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.other.AttributeConstants;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GatewayWebInterceptor implements HandlerInterceptor {

    private final Map<String, Set<String>> requestMethodAndUrl = new HashMap<>();

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (this.urlMatch(request)) {
            //异常处理
            if (ex != null) {
                response.setHeader(AttributeConstants.Http.HEADER_EXCEPTION_CLASS, ex.getClass().getName());
                response.setHeader(AttributeConstants.Http.HEADER_EXCEPTION_MESSAGE, URLEncoder.encode(ex.getMessage(), "UTF-8"));
            }
        }
    }

    public void addUrl(Set<String> urls, String requestMethod) {
        if (CollUtil.isNotEmpty(urls)) {
            requestMethodAndUrl.computeIfAbsent(requestMethod.toUpperCase(), k -> new HashSet<>());
            requestMethodAndUrl.get(requestMethod.toUpperCase()).addAll(urls);
        }
    }

    /**
     * url匹配
     *
     * @param request
     * @return
     */
    private boolean urlMatch(HttpServletRequest request) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        Set<String> urls = requestMethodAndUrl.get(method.toUpperCase());
        if (CollUtil.isNotEmpty(urls)) {
            AntPathMatcher matcher = new AntPathMatcher();

            for (String url : urls) {
                if (matcher.match(url, requestURI)) {
                    return true;
                }
            }
        }

        return false;
    }
}
