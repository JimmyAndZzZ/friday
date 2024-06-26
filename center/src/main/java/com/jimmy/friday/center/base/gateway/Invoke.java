package com.jimmy.friday.center.base.gateway;

import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;

import java.util.Map;

public interface Invoke {

    boolean heartbeat(Service service);

    String invoke(Service service, Method method, Map<String, String> args) throws Exception;

    ServiceTypeEnum type();
}
    