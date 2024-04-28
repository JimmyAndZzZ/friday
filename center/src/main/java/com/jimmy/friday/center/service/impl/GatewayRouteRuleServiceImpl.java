package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayRouteRuleDao;
import com.jimmy.friday.center.entity.GatewayRouteRule;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceMethod;
import com.jimmy.friday.center.service.GatewayRouteRuleService;
import com.jimmy.friday.center.service.GatewayServiceMethodService;
import com.jimmy.friday.center.service.GatewayServiceService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (GatewayRouteRule)表服务实现类
 *
 * @author makejava
 * @since 2023-12-14 18:11:02
 */
@Service("gatewayRouteRuleService")
public class GatewayRouteRuleServiceImpl extends ServiceImpl<GatewayRouteRuleDao, GatewayRouteRule> implements GatewayRouteRuleService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Override
    public List<GatewayRouteRule> getRouteRules(String applicationName, String type, String version, String methodId, String methodCode) {
        GatewayService gatewayService = gatewayServiceService.getGatewayService(applicationName, type, version, null);
        if (gatewayService == null) {
            return Lists.newArrayList();
        }

        GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethodService.queryByMethod(methodCode, methodId, gatewayService.getId());

        List<GatewayRouteRule> cache = attachmentCache.attachmentList(RedisConstants.Gateway.ROUTE_RULE_CACHE, GatewayRouteRule.class, this::list);
        if (CollUtil.isEmpty(cache)) {
            return Lists.newArrayList();
        }

        return cache.stream().filter(bean -> {
            if (!bean.getServiceId().equals(gatewayService.getId())) {
                return false;
            }

            if (bean.getMethodId() != null && gatewayServiceMethod != null && !(bean.getMethodId().equals(gatewayServiceMethod.getId()))) {
                return false;
            }

            return YesOrNoEnum.YES.getCode().equals(bean.getEnabled());
        }).collect(Collectors.toList());
    }

    @Override
    public boolean save(GatewayRouteRule gatewayRouteRule) {
        boolean save = super.save(gatewayRouteRule);

        if (save) {
            this.attachmentCache.attachList(RedisConstants.Gateway.ROUTE_RULE_CACHE, gatewayRouteRule);
        }

        return save;
    }
}

