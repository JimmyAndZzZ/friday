package com.jimmy.friday.framework.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.GatewayInvoke;
import com.jimmy.friday.framework.annotation.gateway.GatewayService;
import com.jimmy.friday.framework.utils.ClassUtil;
import com.jimmy.friday.framework.utils.JsonUtil;
import lombok.Data;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvokeSupport {

    private final Map<String, InvokeObject> fallbackInvokeObject = new HashMap<>();

    private final Map<String, InvokeObject> implementationInvokeObject = new HashMap<>();

    public void putInvokeInterfaceObject(String interfaceName, Method method) {
        if (!implementationInvokeObject.containsKey(interfaceName)) {
            implementationInvokeObject.put(interfaceName, new InvokeObject());
        }

        implementationInvokeObject.get(interfaceName).getMethods().add(method);
    }

    public void putInvokeFallbackObject(String fallbackClass, Method method) {
        if (!fallbackInvokeObject.containsKey(fallbackClass)) {
            fallbackInvokeObject.put(fallbackClass, new InvokeObject());
        }

        fallbackInvokeObject.get(fallbackClass).getMethods().add(method);
    }

    public void invokeInterfaceImplementationObject(Object bean) {
        Class<?> clazz = bean.getClass();

        InvokeObject fallbackObject = fallbackInvokeObject.get(ClassUtil.getClass(clazz).getName());
        if (fallbackObject != null) {
            fallbackObject.setObject(bean);
        }

        GatewayService annotation = AnnotationUtils.getAnnotation(clazz, GatewayService.class);
        if (annotation == null) {
            return;
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (ArrayUtil.isEmpty(interfaces)) {
            return;
        }

        for (Class<?> anInterface : interfaces) {
            InvokeObject invokeObject = implementationInvokeObject.get(anInterface.getName());
            if (invokeObject != null) {
                invokeObject.setObject(bean);
            }
        }
    }

    public void invoke(GatewayInvoke gatewayInvoke) {
        try {
            String invokeMethod = gatewayInvoke.getInvokeMethod();
            String invokeInterface = gatewayInvoke.getInvokeInterface();
            List<InvokeParam> invokeParams = gatewayInvoke.getInvokeParams();

            InvokeObject invokeObject = gatewayInvoke.getIsFallback() ? fallbackInvokeObject.get(invokeInterface) : implementationInvokeObject.get(invokeInterface);
            if (invokeObject == null || invokeObject.getObject() == null) {
                throw new GatewayException(invokeInterface + "没有提供实现类");
            }

            List<Method> methods = invokeObject.getMethods();
            if (CollUtil.isEmpty(methods)) {
                throw new GatewayException(invokeMethod + "未查询到方法");
            }

            Method method = this.findMethod(gatewayInvoke, methods);
            if (method == null) {
                throw new GatewayException(invokeMethod + "未查询到方法");
            }

            ArrayList<Object> arrays = new ArrayList<>();
            if (CollUtil.isNotEmpty(invokeParams)) {
                Class<?> sourceClass = Class.forName(invokeInterface);
                Type[] genericParameterTypes = method.getGenericParameterTypes();

                for (int i = 0; i < invokeParams.size(); i++) {
                    InvokeParam invokeParam = invokeParams.get(i);
                    Type type = genericParameterTypes[i];

                    String jsonData = invokeParam.getJsonData();

                    if (StrUtil.isEmpty(jsonData)) {
                        arrays.add(null);
                        continue;
                    }

                    arrays.add(JsonUtil.deserialize(jsonData, type, sourceClass));
                }
            }
            //执行方法
            Object invoke = method.invoke(invokeObject.getObject(), CollUtil.isEmpty(arrays) ? null : arrays.toArray());

            if (invoke != null) {
                gatewayInvoke.setJsonResult(JsonUtil.toString(invoke));
            }

            gatewayInvoke.setIsSuccess(true);
            gatewayInvoke.setError(null);
            gatewayInvoke.setExceptionClass(null);
        } catch (GatewayException e) {
            gatewayInvoke.setIsSuccess(false);
            gatewayInvoke.setError(e.getMessage());
            gatewayInvoke.setExceptionClass(GatewayException.class.getName());
        } catch (Exception e) {
            gatewayInvoke.setIsSuccess(false);
            gatewayInvoke.setError(e.getMessage());
            gatewayInvoke.setExceptionClass(e.getClass().getName());
        }
    }

    /**
     * 获取匹配方法
     *
     * @param gatewayInvoke
     * @param methods
     * @return
     */
    private Method findMethod(GatewayInvoke gatewayInvoke, List<Method> methods) {
        String invokeMethod = gatewayInvoke.getInvokeMethod();
        List<InvokeParam> invokeParams = gatewayInvoke.getInvokeParams();

        for (Method method : methods) {
            if (method.getName().equals(invokeMethod)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                //参数都为空
                if (ArrayUtil.isEmpty(parameterTypes) && ArrayUtil.isEmpty(invokeParams)) {
                    return method;
                }
                //判断参数是否相同
                List<Class<?>> classes = CollUtil.toList(parameterTypes);
                List<String> collect = classes.stream().map(Class::getName).collect(Collectors.toList());

                if (CollUtil.isEqualList(collect, invokeParams.stream().map(InvokeParam::getClassName).collect(Collectors.toList()))) {
                    return method;
                }
            }
        }

        return null;
    }


    @Data
    private static class InvokeObject implements Serializable {

        private Object object;

        private List<Method> methods = new ArrayList<>();
    }
}
