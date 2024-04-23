package com.jimmy.friday.center.other;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.jimmy.friday.center.annotation.Async;
import com.jimmy.friday.center.config.FridayConfigProperties;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.core.ProtocolFactory;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import com.jimmy.friday.protocol.registered.BaseRegistered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

@Slf4j
@Component
public class AsyncBeanProcessor implements BeanPostProcessor {

    @Autowired
    private ProtocolFactory protocolFactory;

    @Autowired
    private FridayConfigProperties fridayConfigProperties;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            Class<?> clazz = bean.getClass();
            Method[] methods = clazz.getMethods();
            if (ArrayUtil.isNotEmpty(methods)) {
                boolean isNeedProxy = false;

                for (Method method : methods) {
                    Async annotation = AnnotationUtils.getAnnotation(method, Async.class);
                    if (annotation == null) {
                        continue;
                    }

                    Type[] genericParameterTypes = method.getGenericParameterTypes();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (ArrayUtil.isEmpty(parameterTypes) || parameterTypes.length > 1) {
                        continue;
                    }

                    isNeedProxy = true;
                    Class<?> parameterType = parameterTypes[0];

                    ProtocolProperty protocol = fridayConfigProperties.getProtocol();
                    ProtocolEnum cacheProtocolType = fridayConfigProperties.getProtocolType();
                    BaseRegistered registeredByType = protocolFactory.getRegisteredByType(cacheProtocolType, protocol);

                    Protocol info = new Protocol();
                    info.setGroupId(annotation.groupId());
                    info.setTopic(annotation.topic());
                    info.setBatchSize(100);
                    registeredByType.inputGene(info, message -> {
                        if (StrUtil.isNotEmpty(message)) {
                            if (Collection.class.isAssignableFrom(parameterType)) {
                                if (ArrayUtil.isNotEmpty(genericParameterTypes)) {
                                    Type genericParameterType = genericParameterTypes[0];
                                    if (genericParameterType instanceof ParameterizedType) {
                                        ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                                        if (ArrayUtil.isNotEmpty(actualTypeArguments)) {
                                            Type actualTypeArgument = actualTypeArguments[0];

                                            Class<?> generic = Class.forName(actualTypeArgument.getTypeName());

                                            Object o = JSON.parseArray(message, generic);
                                            method.invoke(bean, o);
                                        }
                                    }
                                }

                                return null;
                            }

                            Object o = JSON.parseObject(message, parameterType);
                            method.invoke(bean, o);
                        }

                        return null;
                    });
                }

                if (isNeedProxy) {
                    AsyncCglibProxy asyncCglibProxy = new AsyncCglibProxy(bean, protocolFactory, fridayConfigProperties);
                    //创建加强器
                    Enhancer enhancer = new Enhancer();
                    //为加强器指定要代理的业务类
                    enhancer.setSuperclass(clazz);
                    //设置回调
                    enhancer.setCallback(asyncCglibProxy);
                    return enhancer.create();
                }
            }

            return bean;
        } catch (Exception e) {
            log.error("代理异步失败", e);
            throw new IllegalArgumentException(e);
        }
    }
}
