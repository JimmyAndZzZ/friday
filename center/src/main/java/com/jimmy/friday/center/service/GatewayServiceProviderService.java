package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceProvider;

import java.util.List;

/**
 * (GatewayServiceProvider)表服务接口
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
public interface GatewayServiceProviderService extends IService<GatewayServiceProvider> {

    void register(GatewayService gatewayService, Service service);

    void update(Service service, Boolean isManual);

    boolean updateStatus(String status, Long id);

    List<GatewayServiceProvider> queryByServiceId(Long serviceId);

    GatewayServiceProvider query(Service service);
}

