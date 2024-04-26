package com.jimmy.friday.center.core.gateway.support;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.base.Invoke;
import com.jimmy.friday.center.event.InvokeEvent;
import com.jimmy.friday.center.core.gateway.invoke.BaseInvoke;
import com.jimmy.friday.center.service.GatewayServiceConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InvokeSupport implements Invoke, Initialize, ApplicationListener<InvokeEvent> {

    private final Map<ServiceTypeEnum, Invoke> invokeMap = Maps.newHashMap();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GatewayServiceConsumerService gatewayServiceConsumerService;

    @Override
    public boolean heartbeat(Service service) {
        Invoke invoke = invokeMap.get(service.serviceType());
        if (invoke == null) {
            throw new GatewayException("该类型调用类不存在");
        }

        return invoke.heartbeat(service);
    }

    @Override
    public String invoke(Service service, Method method, Map<String, String> args) throws Exception {
        Invoke invoke = invokeMap.get(service.serviceType());
        if (invoke == null) {
            throw new GatewayException("该类型调用类不存在");
        }

        return invoke.invoke(service, method, args);
    }

    @Override
    public ServiceTypeEnum type() {
        return null;
    }

    @Override
    public void init() throws Exception {
        Map<String, BaseInvoke> beansOfType = applicationContext.getBeansOfType(BaseInvoke.class);
        beansOfType.values().forEach(bean -> invokeMap.put(bean.type(), bean));
    }

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public void onApplicationEvent(InvokeEvent event) {
        gatewayServiceConsumerService.save(event.getServiceId(), event.getIpAddress(), event.getAppId(), event.getClientName(), event.getProviderId());
    }
}
