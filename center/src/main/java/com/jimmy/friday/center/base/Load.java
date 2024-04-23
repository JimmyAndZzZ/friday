package com.jimmy.friday.center.base;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.LoadTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;

import java.util.List;

public interface Load {

    Service load(List<Service> serviceList, ServiceTypeEnum serviceTypeEnum);

    LoadTypeEnum type();
}
