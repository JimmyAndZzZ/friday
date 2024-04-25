package com.jimmy.friday.center.other;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.center.core.gateway.RegisterCenter;
import com.jimmy.friday.center.event.SuspectedFailEvent;
import com.jimmy.friday.center.support.RegisterSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SuspectedFailEventListener implements ApplicationListener<SuspectedFailEvent> {

    @Autowired
    private RegisterSupport registerSupport;

    @Override
    public void onApplicationEvent(SuspectedFailEvent event) {
        Service service = event.getService();
        if (service != null) {
            RegisterCenter center = registerSupport.get(service.serviceType());
            if (center != null) {
                center.addInvokeFail(service);
            }
        }
    }
}
