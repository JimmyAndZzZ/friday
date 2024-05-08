package com.jimmy.friday.center.core.gateway.support;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.CallbackTypeEnum;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.center.base.gateway.Callback;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.event.CallbackEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CallbackSupport implements Initialize, ApplicationListener<CallbackEvent> {

    private final Map<ServiceTypeEnum, Callback> callbackMap = Maps.newHashMap();

    @Override
    public void onApplicationEvent(CallbackEvent event) {
        Service service = event.getService();
        ServiceTypeEnum serviceTypeEnum = event.getServiceTypeEnum();
        CallbackTypeEnum callbackTypeEnum = event.getCallbackTypeEnum();

        Callback callback = callbackMap.get(serviceTypeEnum);
        if (callback == null) {
            return;
        }

        switch (callbackTypeEnum) {
            case REGISTER:
                callback.register(service);
                break;
        }
    }

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        Map<String, Callback> beansOfType = applicationContext.getBeansOfType(Callback.class);
        beansOfType.values().forEach(bean -> callbackMap.put(bean.type(), bean));
    }

    @Override
    public int sort() {
        return 0;
    }
}
