package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayRouteRule;

import java.util.List;

/**
 * (GatewayRouteRule)表服务接口
 *
 * @author makejava
 * @since 2023-12-14 18:11:02
 */
public interface GatewayRouteRuleService extends IService<GatewayRouteRule> {

    List<GatewayRouteRule> getRouteRules(String applicationName, String type, String version, String methodId, String methodCode);

}

