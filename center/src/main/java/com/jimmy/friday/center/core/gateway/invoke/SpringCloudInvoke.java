package com.jimmy.friday.center.core.gateway.invoke;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.Heartbeat;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.center.action.gateway.GatewayHeartbeatAction;
import com.jimmy.friday.center.core.gateway.GatewaySession;
import com.jimmy.friday.center.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SpringCloudInvoke extends BaseInvoke {

    @Autowired
    private HttpInvoke httpInvoke;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GatewayHeartbeatAction gatewayHeartbeatAction;

    @Override
    public boolean heartbeat(Service service) {
        Heartbeat heartbeat = gatewayHeartbeatAction.heartbeat(new Heartbeat(IdUtil.getSnowflake(1, 1).nextId()), service.getApplicationId());
        return heartbeat != null;
    }

    @Override
    public String invoke(Service service, Method method, Map<String, String> args) throws Exception {
        log.info("准备调用SpringCloud,name:{},url:{},args:{}", service.getName(), method.getHttpUrl(), args);

        if (GatewaySession.getIsRoute()) {
            return httpInvoke.invoke(service, method, args);
        }

        int i = 0;
        while (true) {
            try {
                return this.springCloudInvoke(method, args);
            } catch (HttpException e) {
                if (i++ >= method.getRetry()) {
                    throw e;
                }
            } catch (RestClientResponseException e) {
                throw this.errorHandler(e);
            }
        }
    }

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.SPRING_CLOUD;
    }

    /**
     * 执行请求
     *
     * @param method
     * @param args
     * @return
     * @throws Exception
     */
    private String springCloudInvoke(Method method, Map<String, String> args) throws Exception {
        List<Param> params = method.getParams();
        Set<String> httpUrl = method.getHttpUrl();
        List<Param> httpPathParams = method.getHttpPathParams();
        String httpRequestMethod = method.getHttpRequestMethod();

        StringBuilder url = new StringBuilder(StrUtil.builder().append("http://").append(method.getFeignName()).append(this.getUrl(httpUrl, httpPathParams, args)).toString());

        switch (httpRequestMethod.toUpperCase()) {
            case "POST":
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> request;

                if (method.getHttpIsContainBody()) {
                    String requestBody = args.get(AttributeConstants.Http.HTTP_BODY);
                    if (StrUtil.isNotEmpty(requestBody)) {
                        request = new HttpEntity<>(requestBody, headers);
                    } else {
                        List<String> jsonNodes = Lists.newArrayList();

                        Set<String> keySet = args.keySet();
                        for (String string : keySet) {
                            String value = args.get(string);
                            if (StrUtil.isNotEmpty(value)) {
                                jsonNodes.add("\"" + string + "\":" + value);
                            }
                        }

                        request = new HttpEntity<>("{" + CollUtil.join(jsonNodes, ",") + "}", headers);
                    }
                } else {
                    request = new HttpEntity<>(headers);
                }

                return restTemplate.postForObject(URI.create(url.toString()), request, String.class);
            case "GET":
                url.append("?t=").append(System.currentTimeMillis());

                if (CollUtil.isNotEmpty(params)) {
                    for (Param param : params) {
                        String name = param.getName();
                        String value = this.getParamValue(param, args);
                        if (StrUtil.isNotEmpty(value)) {
                            url.append("&").append(name).append("=").append(value);
                        }
                    }
                }

                return restTemplate.getForObject(URI.create(url.toString()), String.class);
            default:
                throw new GatewayException("不支持HTTP请求方式");
        }
    }

    /**
     * 获取参数值
     *
     * @param param
     * @param args
     * @return
     */
    private String getParamValue(Param param, Map<String, String> args) {
        String value = args.get(param.getName());
        if (StrUtil.isEmpty(value)) {
            return null;
        }

        String type = param.getType();
        return JsonUtil.parseObject(value, ClassUtil.loadClass(type)).toString();
    }

    /**
     * 获取http请求url
     *
     * @param httpUrl
     * @param httpPathParams
     * @return
     */
    private String getUrl(Set<String> httpUrl, List<Param> httpPathParams, Map<String, String> args) {
        if (CollUtil.isEmpty(httpUrl)) {
            return null;
        }

        String url = httpUrl.stream().findFirst().get();

        if (CollUtil.isNotEmpty(httpPathParams)) {
            for (Param httpPathParam : httpPathParams) {
                String name = httpPathParam.getName();

                String s = this.getParamValue(httpPathParam, args);
                if (StrUtil.isEmpty(s)) {
                    throw new GatewayException("url路径中字段" + name + "未赋值");
                }

                String regex = "\\{" + name + "\\}";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(url);
                url = matcher.replaceAll(s);
            }
        }

        return url;
    }

    /**
     * 异常处理
     *
     * @param restClientResponseException
     */
    private Exception errorHandler(RestClientResponseException restClientResponseException) throws Exception {
        HttpHeaders responseHeaders = restClientResponseException.getResponseHeaders();
        String responseBodyAsString = restClientResponseException.getResponseBodyAsString();

        String exceptionClass = responseHeaders.getFirst(AttributeConstants.Http.HEADER_EXCEPTION_CLASS);
        String exceptionMessage = responseHeaders.getFirst(AttributeConstants.Http.HEADER_EXCEPTION_MESSAGE);

        if (StrUtil.isAllNotEmpty(exceptionClass, exceptionMessage)) {
            return super.geneException(URLDecoder.decode(exceptionMessage, "UTF-8"), exceptionClass);
        }

        if (StrUtil.isNotEmpty(responseBodyAsString)) {
            JsonNode parse = JsonUtil.parse(responseBodyAsString);
            if (parse != null) {
                JsonNode message = parse.get("message");
                if (message != null) {
                    String text = message.asText();
                    if (StrUtil.isNotEmpty(text)) {
                        return new GatewayException(text);
                    }
                }
            }
        }

        return new GatewayException(restClientResponseException.getMessage());
    }
}
