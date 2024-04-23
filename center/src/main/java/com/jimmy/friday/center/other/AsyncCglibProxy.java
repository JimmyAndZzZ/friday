package com.jimmy.friday.center.other;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.jimmy.friday.center.annotation.Async;
import com.jimmy.friday.center.config.GatewayConfigProperties;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.core.ProtocolFactory;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import com.jimmy.friday.protocol.registered.BaseRegistered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

@Slf4j
public class AsyncCglibProxy implements MethodInterceptor {

    private final Object target;

    private final ProtocolFactory protocolFactory;

    private final GatewayConfigProperties gatewayConfigProperties;

    public AsyncCglibProxy(Object target, ProtocolFactory protocolFactory, GatewayConfigProperties gatewayConfigProperties) {
        this.target = target;
        this.protocolFactory = protocolFactory;
        this.gatewayConfigProperties = gatewayConfigProperties;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Async annotation = AnnotationUtils.getAnnotation(method, Async.class);
        if (annotation == null) {
            return method.invoke(target, objects);
        }

        if (ArrayUtil.isEmpty(objects) || objects.length > 1 || objects[0] == null) {
            return method.invoke(target, objects);
        }

        ProtocolProperty protocol = gatewayConfigProperties.getProtocol();
        ProtocolEnum cacheProtocolType = gatewayConfigProperties.getProtocolType();

        BaseRegistered registeredByType = protocolFactory.getRegisteredByType(cacheProtocolType, protocol);

        Protocol info = new Protocol();
        info.setGroupId(annotation.groupId());
        info.setTopic(annotation.topic());
        Output output = registeredByType.registeredClient(info);

        Object object = objects[0];
        output.send(JSON.toJSONString(object));
        return null;
    }
}
