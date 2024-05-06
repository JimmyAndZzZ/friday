package com.jimmy.friday.center.core.gateway.load;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.LoadTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.center.base.gateway.Load;
import com.jimmy.friday.center.core.gateway.RegisterCenter;
import com.jimmy.friday.center.core.gateway.support.RegisterSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SingleThreadLoad implements Load {

    @Autowired
    private RegisterSupport registerSupport;

    @Override
    public Service load(List<Service> serviceList, ServiceTypeEnum serviceTypeEnum) {
        RegisterCenter registerCenter = registerSupport.get(serviceTypeEnum);
        if (registerCenter == null) {
            return null;
        }

        for (Service service : serviceList) {
            String serviceId = service.getServiceId();

            Boolean result = registerCenter.lockService(serviceId);
            if (result != null && result) {
                return service;
            }
        }

        return null;
    }

    @Override
    public LoadTypeEnum type() {
        return LoadTypeEnum.SINGLE_THREAD;
    }
}
