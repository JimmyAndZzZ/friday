package com.jimmy.friday.framework.core;

import com.jimmy.friday.boot.message.ClientDisconnect;
import com.jimmy.friday.framework.base.Callback;
import com.jimmy.friday.framework.support.TransmitSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class DestroyHook {

    private final ConfigLoad configLoad;

    private final TransmitSupport transmitSupport;

    private final ApplicationContext applicationContext;

    public DestroyHook(ConfigLoad configLoad, TransmitSupport transmitSupport, ApplicationContext applicationContext) {
        this.configLoad = configLoad;
        this.transmitSupport = transmitSupport;
        this.applicationContext = applicationContext;
    }

    public void showdown() {
        log.info("正在关闭容器");

        applicationContext.getBeansOfType(Callback.class).values().forEach(Callback::close);

        ClientDisconnect clientDisconnect = new ClientDisconnect();
        clientDisconnect.setId(configLoad.getId());
        transmitSupport.send(clientDisconnect);
    }
}
