package com.jimmy.friday.boot.core.gateway;

import com.jimmy.friday.boot.other.GlobalConstants;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Method implements Serializable {

    private String desc;

    private String example;

    private String methodId;

    private String name;

    private String interfaceName;

    private Integer timeout = GlobalConstants.DEFAULT_TIMEOUT;

    private Integer retry = 0;

    private String returnType;

    private String returnTypeDisplay;

    private String dubboVersion;

    private Set<String> httpUrl;

    private String feignName;

    private String httpRequestMethod;

    private Boolean isSync = true;

    private Boolean httpIsContainBody = false;

    private Boolean httpIsContainFile = false;

    private String httpFileParamName;

    private List<Param> params = new ArrayList<>();

    private List<Param> httpPathParams = new ArrayList<>();

    private String fallbackClass;

    private String fallbackMethod;

    private Set<String> fallbackIgnoreExceptions = new HashSet<>();

    public void setTimeout(Integer timeout) {
        if (timeout != null && timeout > 0) {
            this.timeout = timeout;
        }
    }

    /**
     * 参数填充
     *
     * @param method
     */
    public void paramFill(java.lang.reflect.Method method) {
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

                this.getParams().add(param);
            }
        }

        Class<?> returnClass = method.getReturnType();
        if (returnClass != null) {
            this.returnType = returnClass.getName();
            this.returnTypeDisplay = returnClass.getTypeName();

            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;
                this.returnTypeDisplay = parameterizedType.toString();
            }
        }
    }

    public String key() {
        StringBuilder param = new StringBuilder();

        if (!params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                Param p = params.get(i);

                param.append(i).append(":").append(p.getDisplay());
            }
        }

        return new StringBuilder().append(name).append(":").append(interfaceName).append(":").append(returnTypeDisplay).append("&").append(param).toString();
    }

    public void setRetry(Integer retry) {
        if (retry != null && retry > 0) {
            this.retry = retry;
        }
    }

    public Method clone(Integer timeout, Integer retry) {
        Method invokeMethod = new Method();
        invokeMethod.setMethodId(this.getMethodId());
        invokeMethod.setTimeout(timeout);
        invokeMethod.setReturnTypeDisplay(this.getReturnTypeDisplay());
        invokeMethod.setReturnType(this.getReturnType());
        invokeMethod.setName(this.getName());
        invokeMethod.setRetry(retry);
        invokeMethod.setDubboVersion(this.getDubboVersion());
        invokeMethod.setParams(this.getParams());
        invokeMethod.setIsSync(this.getIsSync());
        invokeMethod.setHttpUrl(this.getHttpUrl());
        invokeMethod.setFeignName(this.getFeignName());
        invokeMethod.setHttpRequestMethod(this.getHttpRequestMethod());
        invokeMethod.setHttpPathParams(this.getHttpPathParams());
        invokeMethod.setInterfaceName(this.getInterfaceName());
        invokeMethod.setHttpIsContainBody(this.getHttpIsContainBody());
        invokeMethod.setHttpIsContainFile(this.getHttpIsContainFile());
        invokeMethod.setFallbackClass(this.getFallbackClass());
        invokeMethod.setFallbackMethod(this.getFallbackMethod());
        invokeMethod.setFallbackIgnoreExceptions(this.getFallbackIgnoreExceptions());
        invokeMethod.setHttpFileParamName(this.getHttpFileParamName());
        return invokeMethod;
    }
}
