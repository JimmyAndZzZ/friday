package com.jimmy.friday.center.base;

import com.jimmy.friday.boot.enums.ChargeTypeEnum;
import com.jimmy.friday.center.api.ApiContext;

import java.math.BigDecimal;

public interface Cost {

    BigDecimal calculate(Long costStrategyId, String appId, String action, ApiContext apiContext);

    ChargeTypeEnum type();
}
