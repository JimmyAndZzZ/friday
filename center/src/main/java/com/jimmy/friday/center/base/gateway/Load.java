package com.jimmy.friday.center.base.gateway;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.LoadTypeEnum;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;

import java.util.List;

public interface Load {

    Service load(List<Service> serviceList, ServiceTypeEnum serviceTypeEnum);

    LoadTypeEnum type();
}
