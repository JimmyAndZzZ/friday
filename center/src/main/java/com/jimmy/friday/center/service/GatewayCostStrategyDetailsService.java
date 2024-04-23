package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayCostStrategyDetails;

import java.util.Collection;
import java.util.List;

/**
 * (GatewayCostStrategyDetails)表服务接口
 *
 * @author makejava
 * @since 2024-01-02 17:53:21
 */
public interface GatewayCostStrategyDetailsService extends IService<GatewayCostStrategyDetails> {

    List<GatewayCostStrategyDetails> queryByCostStrategyIds(Collection<Long> costStrategyIds);

    List<GatewayCostStrategyDetails> queryByCostStrategyId(Long costStrategyId);
}

