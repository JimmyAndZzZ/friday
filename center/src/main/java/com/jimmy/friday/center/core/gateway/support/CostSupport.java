package com.jimmy.friday.center.core.gateway.support;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.gateway.ChargeTypeEnum;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.center.core.gateway.api.ApiContext;
import com.jimmy.friday.center.base.gateway.Cost;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.boot.other.ApiConstants;
import com.jimmy.friday.center.utils.Assert;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CostSupport implements Initialize {

    private final Map<ChargeTypeEnum, Cost> costMap = Maps.newHashMap();

    @Autowired
    private AttachmentCache attachmentCache;

    public BigDecimal calculate(String chargeType, Long costStrategyId, String appId, String action, ApiContext apiContext) throws Exception {
        ChargeTypeEnum chargeTypeEnum = ChargeTypeEnum.queryByType(chargeType);

        Assert.state(chargeTypeEnum != null, ExceptionEnum.SYSTEM_ERROR, "扣费计算失败");
        return costMap.get(chargeTypeEnum).calculate(costStrategyId, appId, action, apiContext);
    }

    public void rollback(String appId, String action, ApiContext apiContext) {
        String key = StrUtil.builder().append(RedisConstants.Gateway.API_INVOKE_COUNT).append(appId).append(":").append(action).toString();

        Integer anInt = apiContext.getInt(ApiConstants.CONTEXT_INCREMENT_COUNT);
        if (anInt != null) {
            attachmentCache.decrement(key, anInt.longValue());
        }
    }

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        Map<String, Cost> beansOfType = applicationContext.getBeansOfType(Cost.class);
        beansOfType.values().forEach(bean -> costMap.put(bean.type(), bean));
    }

    @Override
    public int sort() {
        return 0;
    }
}


