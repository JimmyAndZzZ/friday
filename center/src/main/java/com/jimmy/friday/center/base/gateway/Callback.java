package com.jimmy.friday.center.base.gateway;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;

public interface Callback {

    void register(Service service);

    ServiceTypeEnum type();
}
