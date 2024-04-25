package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.center.base.Obtain;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayCostStrategyDetailsDao;
import com.jimmy.friday.center.entity.GatewayCostStrategyDetails;
import com.jimmy.friday.center.entity.GatewayRouteRule;
import com.jimmy.friday.center.service.GatewayCostStrategyDetailsService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * (GatewayCostStrategyDetails)表服务实现类
 *
 * @author makejava
 * @since 2024-01-02 17:53:21
 */
@Service("gatewayCostStrategyDetailsService")
public class GatewayCostStrategyDetailsServiceImpl extends ServiceImpl<GatewayCostStrategyDetailsDao, GatewayCostStrategyDetails> implements GatewayCostStrategyDetailsService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public List<GatewayCostStrategyDetails> queryByCostStrategyId(Long costStrategyId) {
        return attachmentCache.attachmentList(RedisConstants.Gateway.COST_STRATEGY_DETAILS_CACHE + costStrategyId, GatewayCostStrategyDetails.class, () -> {
            QueryWrapper<GatewayCostStrategyDetails> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("strategy_id", costStrategyId);
            return list(queryWrapper);
        });
    }

    @Override
    public List<GatewayCostStrategyDetails> queryByCostStrategyIds(Collection<Long> costStrategyIds) {
        QueryWrapper<GatewayCostStrategyDetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("strategy_id", costStrategyIds);
        return list(queryWrapper);
    }
}

