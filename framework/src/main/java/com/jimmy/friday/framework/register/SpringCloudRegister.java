package com.jimmy.friday.framework.register;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.framework.annotation.gateway.Api;
import com.jimmy.friday.framework.annotation.gateway.Condition;
import com.jimmy.friday.framework.condition.SpringCloudCondition;
import com.jimmy.friday.framework.other.gateway.GatewayWebInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@Condition(condition = SpringCloudCondition.class)
public class SpringCloudRegister extends BaseRegister {

    @Autowired
    private Environment environment;

    @Autowired
    private GatewayWebInterceptor gatewayWebInterceptor;

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.SPRING_CLOUD;
    }

    @Override
    public void register() throws Exception {

    }

    @Override
    protected Service geneService() throws Exception {
        Service service = super.geneService();
        String contextPath = environment.getProperty("server.servlet.context-path");
        if (StrUtil.isNotEmpty(contextPath)) {
            service.putAttribute(AttributeConstants.Http.SERVER_SERVLET_CONTEXT_PATH, this.urlHandler(contextPath));
        }

        return service;
    }

    @Override
    public void collectMethod(Class<?> clazz) throws Exception {
        String applicationName = environment.getProperty("spring.application.name");
        if (StrUtil.isEmpty(applicationName)) {
            throw new GatewayException("应用名为空");
        }

        if (!clazz.isInterface()) {
            return;
        }

        FeignClient annotation = AnnotationUtils.getAnnotation(clazz, FeignClient.class);
        if (annotation == null) {
            return;
        }

        Method[] methods = clazz.getMethods();
        if (ArrayUtil.isEmpty(methods)) {
            return;
        }

        for (java.lang.reflect.Method method : methods) {
            Api api = AnnotationUtils.getAnnotation(method, Api.class);
            if (api == null) {
                continue;
            }

            com.jimmy.friday.boot.core.gateway.Method m = this.mapper(api, method, AnnotationUtils.getAnnotation(clazz, RequestMapping.class));
            if (m == null) {
                continue;
            }

            gatewayWebInterceptor.addUrl(m.getHttpUrl(), m.getHttpRequestMethod());
            m.setFeignName(applicationName);
            m.setInterfaceName(clazz.getName());
            super.fallbackHandler(clazz, api, m, method);
            this.addMethod(m);
        }
    }

    @Override
    public TypeFilter getTypeFilter() {
        return new AnnotationTypeFilter(FeignClient.class);
    }

    /**
     * 获取http映射方法
     *
     * @param method
     * @return
     */
    private com.jimmy.friday.boot.core.gateway.Method mapper(Api api, Method method, RequestMapping annotation) {
        String[] rootPath = annotation == null ? new String[]{} : annotation.path();
        if (ArrayUtil.isEmpty(rootPath)) {
            rootPath = new String[]{StrUtil.EMPTY};
        }

        GetMapping getMapping = AnnotationUtils.getAnnotation(method, GetMapping.class);
        if (getMapping != null) {
            com.jimmy.friday.boot.core.gateway.Method m = this.geneMethod(method, api, false);
            m.setHttpUrl(this.getPaths(rootPath, getMapping.path()));
            m.setHttpRequestMethod(RequestMethod.GET.toString());
            this.getParamHandler(m, method);
            return m;
        }

        PostMapping postMapping = AnnotationUtils.getAnnotation(method, PostMapping.class);
        if (postMapping != null) {
            com.jimmy.friday.boot.core.gateway.Method m = this.geneMethod(method, api, false);
            m.setHttpUrl(this.getPaths(rootPath, postMapping.path()));
            m.setHttpRequestMethod(RequestMethod.POST.toString());
            this.postParamHandler(m, method);
            return m;
        }

        RequestMapping requestMapping = AnnotationUtils.getAnnotation(method, RequestMapping.class);
        if (requestMapping != null) {
            RequestMethod[] requestMethods = requestMapping.method();
            if (ArrayUtil.isEmpty(requestMethods)) {
                return null;
            }

            Set<String> paths = this.getPaths(rootPath, requestMapping.path());
            for (RequestMethod requestMethod : requestMethods) {
                com.jimmy.friday.boot.core.gateway.Method m = this.geneMethod(method, api, false);

                switch (requestMethod) {
                    case GET:
                        m.setHttpUrl(paths);
                        m.setHttpRequestMethod(requestMethod.toString());
                        this.getParamHandler(m, method);
                        return m;
                    case POST:
                        m.setHttpUrl(paths);
                        m.setHttpRequestMethod(requestMethod.toString());
                        this.postParamHandler(m, method);
                        return m;
                    default:
                        return null;
                }
            }
        }

        return null;
    }


    /**
     * 获取url集合
     *
     * @param rootPath
     * @param path
     * @return
     */
    private Set<String> getPaths(String[] rootPath, String[] path) {
        String contextPath = StrUtil.emptyToDefault(environment.getProperty("server.servlet.context-path"), StrUtil.EMPTY);

        if (ArrayUtil.isEmpty(path)) {
            path = new String[]{StrUtil.EMPTY};
        }

        String first = this.urlHandler(contextPath);

        Set<String> paths = new HashSet<>();
        for (String root : rootPath) {
            String second = this.urlHandler(root);

            for (String p : path) {
                String third = this.urlHandler(p);

                StringBuilder sb = new StringBuilder();
                sb.append("/").append(first);

                if (StrUtil.isNotEmpty(first)) {
                    sb.append("/");
                }

                sb.append(second);

                if (StrUtil.isNotEmpty(second)) {
                    sb.append("/");
                }

                sb.append(third);

                paths.add(sb.toString());
            }
        }

        return paths;
    }

    /**
     * 增加开头斜杠
     *
     * @param s
     * @return
     */
    private String urlHandler(String s) {
        boolean isNeedContinue = false;
        s = StrUtil.trim(s);

        if (StrUtil.isEmpty(s)) {
            return StrUtil.EMPTY;
        }

        if (StrUtil.startWith(s, "/")) {
            isNeedContinue = true;
            s = StrUtil.sub(s, 1, s.length());
        }

        if (StrUtil.endWith(s, "/")) {
            isNeedContinue = true;
            s = StrUtil.sub(s, 0, s.length() - 1);
        }

        return isNeedContinue ? urlHandler(s) : s;
    }


    /**
     * post参数处理
     *
     * @param m
     * @param method
     */
    private void postParamHandler(com.jimmy.friday.boot.core.gateway.Method m, Method method) {
        boolean isRequestBody = false;
        Parameter[] parameters = method.getParameters();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Type parameterType = genericParameterTypes[i];

                String typeName = parameter.getType().getName();

                Param param = new Param();
                param.setName(parameter.getName());
                param.setType(typeName);
                param.setDisplay(typeName);

                if (parameterType instanceof ParameterizedType) {
                    param.setDisplay(parameterType.toString());
                }

                if (!isRequestBody) {
                    RequestBody requestBody = AnnotationUtils.getAnnotation(parameter, RequestBody.class);
                    if (requestBody != null) {
                        isRequestBody = true;
                        m.getParams().add(param);
                        m.setHttpIsContainBody(true);
                    }
                }

                PathVariable pathVariable = AnnotationUtils.getAnnotation(parameter, PathVariable.class);
                if (pathVariable != null) {
                    String value = pathVariable.value();

                    if (StrUtil.isNotEmpty(value)) {
                        param.setName(value);
                    }

                    m.getHttpPathParams().add(param);
                }
            }
        }

        this.returnHandler(m, method);
    }

    /**
     * get参数处理
     *
     * @param m
     * @param method
     */
    private void getParamHandler(com.jimmy.friday.boot.core.gateway.Method m, Method method) {
        Parameter[] parameters = method.getParameters();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Type parameterType = genericParameterTypes[i];

                String typeName = parameter.getType().getName();

                Param param = new Param();
                param.setName(parameter.getName());
                param.setType(typeName);
                param.setDisplay(typeName);

                if (parameterType instanceof ParameterizedType) {
                    param.setDisplay(parameterType.toString());
                }

                RequestParam requestParam = AnnotationUtils.getAnnotation(parameter, RequestParam.class);
                if (requestParam != null) {
                    String value = requestParam.value();

                    if (StrUtil.isNotEmpty(value)) {
                        param.setName(value);
                    }

                    m.getParams().add(param);
                }

                PathVariable pathVariable = AnnotationUtils.getAnnotation(parameter, PathVariable.class);
                if (pathVariable != null) {
                    String value = pathVariable.value();

                    if (StrUtil.isNotEmpty(value)) {
                        param.setName(value);
                    }

                    m.getHttpPathParams().add(param);
                }
            }
        }

        this.returnHandler(m, method);
    }

    /**
     * 返回值处理
     *
     * @param m
     * @param method
     */
    private void returnHandler(com.jimmy.friday.boot.core.gateway.Method m, Method method) {
        Class<?> returnClass = method.getReturnType();
        if (returnClass != null) {
            m.setReturnType(returnClass.getName());
            m.setReturnTypeDisplay(returnClass.getTypeName());

            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;
                m.setReturnTypeDisplay(parameterizedType.toString());
            }
        }
    }
}
