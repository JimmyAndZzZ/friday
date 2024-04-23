package com.jimmy.friday.center.action.gateway;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.CallbackTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ServiceRegister;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.event.CallbackEvent;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.support.RegisterSupport;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceRegisterAction implements Action<ServiceRegister> {

    @Autowired
    private RegisterSupport registerSupport;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void action(ServiceRegister serviceRegister, ChannelHandlerContext channelHandlerContext) {
        List<Service> services = serviceRegister.getServices();
        if (CollUtil.isEmpty(services)) {
            return;
        }

        for (Service service : services) {
            registerSupport.get(service.serviceType()).register(service, serviceRegister.getId());

            CallbackEvent callbackEvent = new CallbackEvent(applicationContext);
            callbackEvent.setService(service);
            callbackEvent.setCallbackTypeEnum(CallbackTypeEnum.REGISTER);
            callbackEvent.setServiceTypeEnum(service.serviceType());
            applicationContext.publishEvent(callbackEvent);
        }

        ChannelHandlerPool.putChannel(channelHandlerContext.channel());
        ChannelHandlerPool.putSession(serviceRegister.getId(), channelHandlerContext.channel().id());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SERVICE_REGISTER;
    }
}
