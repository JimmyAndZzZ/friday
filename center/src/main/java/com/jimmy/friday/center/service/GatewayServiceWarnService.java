package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayServiceWarn;

import java.util.List;

/**
 * (GatewayServiceWarn)表服务接口
 *
 * @author makejava
 * @since 2024-04-16 14:05:52
 */
public interface GatewayServiceWarnService extends IService<GatewayServiceWarn> {

    List<GatewayServiceWarn> queryList(Long serviceId, Long providerId);
}

