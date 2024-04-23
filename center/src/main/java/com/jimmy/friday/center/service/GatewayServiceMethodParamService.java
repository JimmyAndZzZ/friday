package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceMethodParam;

import java.util.List;

/**
 * (GatewayServiceMethodParam)表服务接口
 *
 * @author makejava
 * @since 2024-03-26 17:55:21
 */
public interface GatewayServiceMethodParamService extends IService<GatewayServiceMethodParam> {

    List<GatewayServiceMethodParam> queryByMethodId(Long methodId);

    void removeByServiceId(Long serviceId);

    List<GatewayServiceMethodParam> queryByServiceId(Long serviceId);

    void refreshMethod(GatewayService gatewayService, Service service);
}

