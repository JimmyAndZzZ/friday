package com.jimmy.friday.center.base.gateway;

import com.jimmy.friday.boot.enums.ChargeTypeEnum;
import com.jimmy.friday.center.core.gateway.api.ApiContext;

import java.math.BigDecimal;

public interface Cost {

    BigDecimal calculate(Long costStrategyId, String appId, String action, ApiContext apiContext);

    ChargeTypeEnum type();
}
