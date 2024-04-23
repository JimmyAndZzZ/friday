package com.jimmy.friday.center.invoke;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.ApiConstants;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.center.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HttpInvoke extends BaseInvoke {

    @Override
    public boolean heartbeat(Service service) {
        String serverServletContextPath = service.getStringAttribute(AttributeConstants.Http.SERVER_SERVLET_CONTEXT_PATH);

        StringBuilder sb = new StringBuilder();
        sb.append("http://").append(service.getIpAddress()).append(":").append(service.getPort());

        if (StrUtil.isNotEmpty(serverServletContextPath)) {
            sb.append(StrUtil.SLASH).append(serverServletContextPath);
        }

        sb.append(StrUtil.SLASH).append(service.getApplicationId()).append("/heartbeat");

        String url = sb.toString();
        String s = HttpUtil.get(url);
        return Convert.toBool(s, false);
    }

    @Override
    public String invoke(Service service, Method method, Map<String, String> args) throws Exception {
        log.info("准备调用http,name:{},ip:{},port:{},url:{},args:{}", service.getName(), service.getIpAddress(), service.getPort(), method.getHttpUrl(), args);

        int i = 0;
        while (true) {
            try {
                return this.httpInvoke(service, method, args);
            } catch (HttpException e) {
                if (i++ >= method.getRetry()) {
                    throw e;
                }
            }
        }
    }

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.HTTP;
    }

    /**
     * 执行请求
     *
     * @param service
     * @param method
     * @param args
     * @return
     * @throws Exception
     */
    private String httpInvoke(Service service, Method method, Map<String, String> args) throws Exception {
        List<Param> params = method.getParams();
        Set<String> httpUrl = method.getHttpUrl();
        List<Param> httpPathParams = method.getHttpPathParams();
        String httpRequestMethod = method.getHttpRequestMethod();

        StringBuilder url = new StringBuilder(StrUtil.builder().append("http://").append(service.getIpAddress()).append(":").append(service.getPort()).append(this.getUrl(httpUrl, httpPathParams, args)).toString());

        switch (httpRequestMethod.toUpperCase()) {
            case "POST":
                HttpRequest post = HttpUtil.createPost(url.toString());
                post.timeout(method.getTimeout() * 1000);

                if (method.getHttpIsContainBody()) {
                    String s = args.get(AttributeConstants.Http.HTTP_BODY);
                    if (StrUtil.isNotEmpty(s)) {
                        post.body(s);
                    } else {
                        List<String> jsonNodes = Lists.newArrayList();

                        Set<String> keySet = args.keySet();
                        for (String string : keySet) {
                            String value = args.get(string);
                            if (StrUtil.isNotEmpty(value)) {
                                jsonNodes.add("\"" + string + "\":" + value);
                            }
                        }

                        post.body("{" + CollUtil.join(jsonNodes, ",") + "}");
                    }
                }

                if (method.getHttpIsContainFile()) {
                    if (MapUtil.isEmpty(args)) {
                        throw new GatewayException("该接口需要上传文件");
                    }

                    String filePath = args.get(ApiConstants.CONTEXT_PARAM_FILE_PATH);
                    String fileName = args.get(ApiConstants.CONTEXT_PARAM_FILE_NAME);

                    if (StrUtil.isEmpty(filePath)) {
                        throw new GatewayException("该接口需要上传文件");
                    }

                    File file = FileUtil.newFile(filePath);
                    post.form(method.getHttpFileParamName(), this.read(file), StrUtil.emptyToDefault(fileName, file.getName()));
                }

                HttpResponse postResponse = post.execute();
                int postStatus = postResponse.getStatus();
                if (postStatus != 200) {
                    this.errorHandler(postResponse);
                }

                return postResponse.body();
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

                HttpRequest get = HttpUtil.createGet(url.toString());
                get.timeout(method.getTimeout() * 1000);
                HttpResponse getResponse = get.execute();
                int getStatus = getResponse.getStatus();
                if (getStatus != 200) {
                    this.errorHandler(getResponse);
                }

                return getResponse.body();
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
     * @param postResponse
     */
    private void errorHandler(HttpResponse postResponse) throws Exception {
        String exceptionClass = postResponse.header(AttributeConstants.Http.HEADER_EXCEPTION_CLASS);
        String exceptionMessage = postResponse.header(AttributeConstants.Http.HEADER_EXCEPTION_MESSAGE);

        if (StrUtil.isAllNotEmpty(exceptionClass, exceptionMessage)) {
            throw super.geneException(URLDecoder.decode(exceptionMessage, "UTF-8"), exceptionClass);
        }

        throw new GatewayException("请求失败，http返回码" + postResponse.getStatus());
    }

    /**
     * 读取文件
     *
     * @return
     */
    private byte[] read(File file) {
        try (FileChannel channel = new RandomAccessFile(file, "r").getChannel()) {
            int fileSize = (int) channel.size();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize).load();
            byte[] result = new byte[fileSize];
            if (buffer.remaining() > 0) {
                buffer.get(result, 0, fileSize);
            }
            buffer.clear();
            return result;
        } catch (Exception e) {
            log.error("文件读取失败", e);
            throw new GatewayException("文件读取失败");
        }
    }
}
