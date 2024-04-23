package com.jimmy.friday.center.cost;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.enums.ChargeTypeEnum;
import com.jimmy.friday.center.api.ApiContext;
import com.jimmy.friday.center.entity.GatewayCostStrategyDetails;
import com.jimmy.friday.boot.other.ApiConstants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TimeCost extends BaseCost {

    @Override
    public BigDecimal calculate(Long costStrategyId, String appId, String action, ApiContext apiContext) {
        List<GatewayCostStrategyDetails> gatewayCostStrategyDetails = gatewayCostStrategyDetailsService.queryByCostStrategyId(costStrategyId);
        if (CollUtil.isEmpty(gatewayCostStrategyDetails)) {
            return new BigDecimal(0);
        }

        apiContext.put(ApiConstants.CONTEXT_INCREMENT_COUNT, 1);
        return super.calculate(appId, action, gatewayCostStrategyDetails, 1);
    }

    @Override
    public ChargeTypeEnum type() {
        return ChargeTypeEnum.TIME;
    }
}
