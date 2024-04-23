package com.jimmy.friday.framework.register;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.framework.annotation.Api;
import com.jimmy.friday.framework.annotation.Example;
import com.jimmy.friday.framework.annotation.ParamDesc;
import com.jimmy.friday.framework.base.Register;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.support.InvokeSupport;
import com.jimmy.friday.framework.utils.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseRegister implements Register {

    private final Set<String> methodIds = new HashSet<>();

    private final List<Method> methods = new ArrayList<>();

    private Service service;

    @Autowired
    private ConfigLoad configLoad;

    @Autowired
    private Environment environment;

    @Autowired
    protected InvokeSupport invokeSupport;

    protected String loadConfig(String key) {
        return configLoad.get(key);
    }

    @Override
    public List<Method> getMethods() throws Exception {
        return this.methods;
    }

    @Override
    public Service getService() throws Exception {
        if (service == null) {
            service = this.geneService();
        }

        return service;
    }

    protected void fallbackHandler(Class<?> own, Api api, Method method, java.lang.reflect.Method m) {
        Class<?> clazz = api.fallbackClass();
        String fallbackMethod = api.fallbackMethod();
        Class<? extends Throwable>[] classes = api.ignoreExceptions();

        if (StrUtil.isEmpty(fallbackMethod)) {
            return;
        }

        Class<?> fallbackClass = clazz.equals(void.class) ? own : clazz;

        java.lang.reflect.Method fallback = this.findFallback(fallbackClass, m, fallbackMethod);
        if (fallback == null) {
            throw new GatewayException("未查询到熔断方法");
        }

        invokeSupport.putInvokeFallbackObject(fallbackClass.getName(), fallback);

        method.setFallbackMethod(fallbackMethod);
        method.setFallbackClass(fallbackClass.getName());

        if (ArrayUtil.isNotEmpty(classes)) {
            for (Class<? extends Throwable> exceptionClass : classes) {
                method.getFallbackIgnoreExceptions().add(exceptionClass.getName());
            }
        }
    }

    protected Method geneMethod(java.lang.reflect.Method method, Api api) {
        return this.geneMethod(method, api, true);
    }

    protected Method geneMethod(java.lang.reflect.Method method, Api api, boolean isFillParam) {
        Method m = new Method();
        m.setMethodId(api.id());
        m.setName(method.getName());
        m.setRetry(api.retry());
        m.setTimeout(api.timeout() == 0 ? GlobalConstants.DEFAULT_TIMEOUT : api.timeout());
        m.setDesc(api.desc());

        Example example = AnnotationUtils.getAnnotation(method, Example.class);
        if (example != null) {
            String[] e = example.example();
            if (ArrayUtil.isNotEmpty(e)) {
                m.setExample(ArrayUtil.join(e, "\n"));
            }
        }

        if (isFillParam) {
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

                    ParamDesc annotation = AnnotationUtils.getAnnotation(parameter, ParamDesc.class);
                    if (annotation != null) {
                        param.setDesc(annotation.desc());
                        param.setIsRequire(annotation.isRequire());
                        param.setDefaultValue(annotation.value());
                    }

                    m.getParams().add(param);
                }
            }

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

        return m;
    }


    protected void addMethod(Method method) {
        if (!methodIds.add(method.getMethodId())) {
            throw new GatewayException("方法id重复:" + method.getMethodId());
        }

        methods.add(method);
    }

    protected Service geneService() throws Exception {
        String name = configLoad.getApplicationName();
        if (StrUtil.isEmpty(name)) {
            throw new GatewayException("未配置应用名");
        }

        Service service = new Service();
        service.setVersion(configLoad.getVersion());
        service.setIpAddress(this.getIpAddress());
        service.setName(name);
        service.setPort(Integer.valueOf(environment.getProperty("server.port")));
        service.setType(type().toString());
        return service;
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    protected String getIpAddress() {
        try {
            InetAddress firstNonLoopBackAddress = configLoad.getLocalIpAddress();

            if (firstNonLoopBackAddress != null) {
                return firstNonLoopBackAddress.getHostAddress();
            }

            throw new GatewayException("获取ip地址失败");
        } catch (Exception e) {
            log.error("获取ip地址失败", e);
            throw new GatewayException("获取ip地址失败");
        }
    }


    /**
     * 获取api方法
     *
     * @param clazz
     * @return
     */
    protected List<java.lang.reflect.Method> getMethodWithApi(Class<?> clazz) {
        List<java.lang.reflect.Method> apis = new ArrayList<>();

        java.lang.reflect.Method[] methods = clazz.getMethods();
        if (methods == null || methods.length == 0) {
            return apis;
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces == null || interfaces.length == 0) {
            return apis;
        }

        for (java.lang.reflect.Method method : methods) {
            Api api = AnnotationUtils.getAnnotation(method, Api.class);
            if (api == null) {
                continue;
            }

            apis.add(method);
        }

        return apis;
    }

    /**
     * 匹配接口方法
     *
     * @param method
     * @return
     */
    protected Class<?> getInterfaceFromMethod(Class<?>[] interfaces, java.lang.reflect.Method method) {
        for (Class<?> intf : interfaces) {
            try {
                java.lang.reflect.Method intfMethod = intf.getMethod(method.getName(), method.getParameterTypes());
                if (intfMethod != null) {
                    return intf;
                }
            } catch (NoSuchMethodException e) {
                // Ignore
            }
        }

        return null;
    }

    /**
     * 获取熔断方法
     *
     * @param clazz
     * @param method
     * @param fallbackMethod
     * @return
     */
    private java.lang.reflect.Method findFallback(Class<?> clazz, java.lang.reflect.Method method, String fallbackMethod) {
        java.lang.reflect.Method[] methods = clazz.getMethods();
        if (ArrayUtil.isEmpty(methods)) {
            return null;
        }

        for (java.lang.reflect.Method m : methods) {
            if (m.getName().equals(fallbackMethod)) {
                Class<?> returnType = method.getReturnType();
                Class<?>[] parameterTypes = method.getParameterTypes();

                Class<?> fallbackReturnType = m.getReturnType();
                Class<?>[] fallbackParameterTypes = m.getParameterTypes();
                //判断返回类型是否相同
                if (!ClassUtil.classEquals(returnType, fallbackReturnType)) {
                    continue;
                }
                //参数都为空
                if (ArrayUtil.isEmpty(parameterTypes) && ArrayUtil.isEmpty(fallbackParameterTypes)) {
                    return m;
                }
                //判断参数是否相同
                List<String> source = CollUtil.toList(parameterTypes).stream().map(Class::getName).collect(Collectors.toList());
                List<String> target = CollUtil.toList(fallbackParameterTypes).stream().map(Class::getName).collect(Collectors.toList());

                if (CollUtil.isEqualList(source, target)) {
                    return m;
                }
            }
        }

        return null;
    }
}
