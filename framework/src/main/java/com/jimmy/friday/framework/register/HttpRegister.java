package com.jimmy.friday.framework.register;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.framework.annotation.Api;
import com.jimmy.friday.framework.annotation.Condition;
import com.jimmy.friday.framework.annotation.ParamDesc;
import com.jimmy.friday.framework.condition.HttpCondition;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.other.GatewayWebInterceptor;
import com.jimmy.friday.framework.other.HttpConnector;
import com.jimmy.friday.framework.other.SwaggerAnnotationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@Condition(condition = HttpCondition.class)
public class HttpRegister extends BaseRegister {

    @Autowired
    private ConfigLoad configLoad;

    @Autowired
    private Environment environment;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private GatewayWebInterceptor gatewayWebInterceptor;

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.HTTP;
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
    public void register() throws Exception {
        if (!beanFactory.containsBeanDefinition("_gateway_http_connector")) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServletRegistrationBean.class);
            beanDefinitionBuilder.addConstructorArgValue(new HttpConnector());
            beanDefinitionBuilder.addConstructorArgValue("/" + configLoad.getId() + "/heartbeat");
            // 注册bean
            beanFactory.registerBeanDefinition("_gateway_http_connector", beanDefinitionBuilder.getRawBeanDefinition());
            this.beanFactory.getBean("_gateway_http_connector");
        }
    }

    @Override
    public void collectMethod(Class<?> clazz) throws Exception {
        RestController annotation = AnnotationUtils.getAnnotation(clazz, RestController.class);
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
            m.setInterfaceName(clazz.getName());
            super.fallbackHandler(clazz, api, m, method);
            this.addMethod(m);
        }
    }

    @Override
    public TypeFilter getTypeFilter() {
        return new AnnotationTypeFilter(RestController.class);
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
            this.methodDescHandler(method, m);
            return m;
        }

        PostMapping postMapping = AnnotationUtils.getAnnotation(method, PostMapping.class);
        if (postMapping != null) {
            com.jimmy.friday.boot.core.gateway.Method m = this.geneMethod(method, api, false);
            m.setHttpUrl(this.getPaths(rootPath, postMapping.path()));
            m.setHttpRequestMethod(RequestMethod.POST.toString());
            this.postParamHandler(m, method);
            this.methodDescHandler(method, m);
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
                        this.methodDescHandler(method, m);
                        return m;
                    case POST:
                        m.setHttpUrl(paths);
                        m.setHttpRequestMethod(requestMethod.toString());
                        this.postParamHandler(m, method);
                        this.methodDescHandler(method, m);
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
        boolean isContainFile = false;
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
                //request body参数处理
                if (!isRequestBody) {
                    RequestBody requestBody = AnnotationUtils.getAnnotation(parameter, RequestBody.class);
                    if (requestBody != null) {
                        isRequestBody = true;
                        this.paramDescHandler(parameter, param);
                        m.getParams().add(param);
                        m.setHttpIsContainBody(true);
                    }
                }
                //路径参数处理
                PathVariable pathVariable = AnnotationUtils.getAnnotation(parameter, PathVariable.class);
                if (pathVariable != null) {
                    String value = pathVariable.value();

                    if (StrUtil.isNotEmpty(value)) {
                        param.setName(value);
                    }

                    this.paramDescHandler(parameter, param);
                    m.getHttpPathParams().add(param);
                }
                //是否包含文件
                if (parameter.getType().equals(MultipartFile.class)) {
                    if (isContainFile) {
                        throw new GatewayException("接口只允许上传一个文件");
                    }

                    isContainFile = true;
                    m.setHttpIsContainFile(true);

                    RequestParam requestParam = AnnotationUtils.getAnnotation(parameter, RequestParam.class);
                    m.setHttpFileParamName(requestParam != null ? requestParam.value() : parameter.getName());

                    this.paramDescHandler(parameter, param);
                    m.getParams().add(param);
                }
            }
        }

        this.returnHandler(m, method);
    }

    /**
     * 方法描述处理
     *
     * @param method
     * @param m
     */
    private void methodDescHandler(Method method, com.jimmy.friday.boot.core.gateway.Method m) {
        if (isDependencySwagger()) {
            new SwaggerAnnotationHandler().methodHandler(method, m);
        }
    }

    /**
     * 参数描述处理
     *
     * @param parameter
     * @param param
     */
    private void paramDescHandler(Parameter parameter, Param param) {
        ParamDesc annotation = AnnotationUtils.getAnnotation(parameter, ParamDesc.class);
        if (annotation != null) {
            param.setDesc(annotation.desc());
            param.setIsRequire(annotation.isRequire());
            param.setDefaultValue(annotation.value());
        }

        if (isDependencySwagger()) {
            new SwaggerAnnotationHandler().parameterHandler(parameter, param);
        }
    }

    /**
     * 是否包含swagger依赖
     *
     * @return
     */
    private boolean isDependencySwagger() {
        try {
            return null != Class.forName("io.swagger.annotations.ApiOperation");
        } catch (ClassNotFoundException e) {
            return false;
        }
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

                    this.paramDescHandler(parameter, param);
                    m.getParams().add(param);
                }

                PathVariable pathVariable = AnnotationUtils.getAnnotation(parameter, PathVariable.class);
                if (pathVariable != null) {
                    String value = pathVariable.value();

                    if (StrUtil.isNotEmpty(value)) {
                        param.setName(value);
                    }

                    this.paramDescHandler(parameter, param);
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
