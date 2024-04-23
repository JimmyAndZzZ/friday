package com.jimmy.friday.center.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.aviator.AviatorEvaluator;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.entity.GatewayRouteRule;
import com.jimmy.friday.center.service.GatewayRouteRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RouteElect {

    @Autowired
    private GatewayRouteRuleService gatewayRouteRuleService;

    public List<Service> route(List<Service> services, GatewayRequest gatewayRequest) {
        try {
            String version = gatewayRequest.getVersion();
            String methodId = gatewayRequest.getMethodId();
            String methodCode = gatewayRequest.getMethodCode();
            String serviceName = gatewayRequest.getServiceName();
            String serviceType = gatewayRequest.getServiceType();

            List<GatewayRouteRule> routeRules = gatewayRouteRuleService.getRouteRules(serviceName, serviceType, version, methodId, methodCode);
            if (CollUtil.isEmpty(routeRules)) {
                return services;
            }

            Map<String, Object> consumer = Maps.newHashMap();
            consumer.put("tag", gatewayRequest.getTag());
            consumer.put("consumerName", gatewayRequest.getClientName());
            consumer.put("consumerIp", gatewayRequest.getClientIpAddress());
            GatewayRouteRule gatewayRouteRule = this.matchRouteRule(routeRules, consumer);
            if (gatewayRouteRule == null) {
                return services;
            }

            List<Service> route = Lists.newArrayList();
            for (Service service : services) {
                Integer port = service.getPort();
                String ipAddress = service.getIpAddress();

                Map<String, Object> provider = Maps.newHashMap();
                provider.put("providerPort", port);
                provider.put("providerIp", ipAddress);

                if (Convert.toBool(AviatorEvaluator.execute(gatewayRouteRule.getProviderCondition(), provider), false)) {
                    route.add(service);
                }
            }

            if (CollUtil.isNotEmpty(route)) {
                GatewaySession.setIsRoute(true);
                return route;
            }

            return YesOrNoEnum.YES.getCode().equals(gatewayRouteRule.getIsForce()) ? route : services;
        } catch (Exception e) {
            log.error("路由选择失败", e);
            return services;
        }
    }

    /**
     * 路由匹配
     *
     * @param routeRules
     * @param param
     * @return
     */
    private GatewayRouteRule matchRouteRule(List<GatewayRouteRule> routeRules, Map<String, Object> param) {
        List<GatewayRouteRule> match = Lists.newArrayList();

        for (GatewayRouteRule routeRule : routeRules) {
            String consumerCondition = routeRule.getConsumerCondition();
            if (StrUtil.isEmpty(consumerCondition)) {
                continue;
            }

            if (Convert.toBool(AviatorEvaluator.execute(consumerCondition, param), false)) {
                match.add(routeRule);
            }
        }

        if (CollUtil.isEmpty(match)) {
            return null;
        }

        if (match.size() > 1) {
            match.sort(Comparator.comparingInt(GatewayRouteRule::getPriority).reversed());
        }

        return CollUtil.getFirst(match);
    }
}
