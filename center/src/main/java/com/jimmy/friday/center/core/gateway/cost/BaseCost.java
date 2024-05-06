package com.jimmy.friday.center.core.gateway.cost;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.center.base.gateway.Cost;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.entity.GatewayCostStrategyDetails;
import com.jimmy.friday.center.service.GatewayCostStrategyDetailsService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public abstract class BaseCost implements Cost {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    protected GatewayCostStrategyDetailsService gatewayCostStrategyDetailsService;

    protected BigDecimal calculate(String appId, String action, List<GatewayCostStrategyDetails> details, Integer time) {
        BigDecimal total = new BigDecimal(0);

        String key = StrUtil.builder().append(RedisConstants.Gateway.API_INVOKE_COUNT).append(appId).append(":").append(action).toString();

        for (int i = 0; i < time; i++) {
            Long increment = attachmentCache.increment(key);

            total = total.add(this.calculate(details, increment.intValue()));
        }

        return total;
    }

    /**
     * 计算每次费用
     *
     * @param details
     * @param time
     * @return
     */
    protected BigDecimal calculate(List<GatewayCostStrategyDetails> details, Integer time) {
        if (CollUtil.isEmpty(details)) {
            return BigDecimal.valueOf(0);
        }

        if (details.size() == 1) {
            return details.stream().findFirst().get().getPrice();
        }

        details.sort(Comparator.comparing(GatewayCostStrategyDetails::getPrice));

        int lastValue = 0;
        BigDecimal lastPrice = null;
        for (GatewayCostStrategyDetails gatewayCostStrategyDetails : details) {
            if (time >= lastValue && time < gatewayCostStrategyDetails.getThresholdValue()) {
                return gatewayCostStrategyDetails.getPrice();
            }

            lastValue = gatewayCostStrategyDetails.getThresholdValue();
            lastPrice = gatewayCostStrategyDetails.getPrice();
        }

        return lastPrice;
    }
}
