package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayServiceConsumer;

import java.util.List;

/**
 * (GatewayServiceConsumer)表服务接口
 *
 * @author makejava
 * @since 2024-03-25 15:50:03
 */
public interface GatewayServiceConsumerService extends IService<GatewayServiceConsumer> {

    List<GatewayServiceConsumer> queryByServiceId(Long serviceId);

    GatewayServiceConsumer query(Long serviceId, String ipAddress, String appId, String clientName);

    void save(Long serviceId, String ipAddress, String appId, String clientName, Long providerId);
}

