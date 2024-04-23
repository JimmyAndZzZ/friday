package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayInvokeTrace;

public interface GatewayInvokeTraceService extends IService<GatewayInvokeTrace> {

    IPage<GatewayInvokeTrace> page(Long serviceId, Long providerId, Long methodId, String startDate, String endDate, String appId, Boolean isSuccess, Integer pageNo, Integer pageSize);

}
