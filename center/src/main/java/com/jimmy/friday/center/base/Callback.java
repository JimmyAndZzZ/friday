package com.jimmy.friday.center.base;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;

public interface Callback {

    void register(Service service);

    ServiceTypeEnum type();
}
