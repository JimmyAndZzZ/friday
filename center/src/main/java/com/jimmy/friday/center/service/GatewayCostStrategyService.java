package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayCostStrategy;

import java.io.Serializable;

/**
 * (GatewayCostStrategy)表服务接口
 *
 * @author makejava
 * @since 2024-01-02 17:53:21
 */
public interface GatewayCostStrategyService extends IService<GatewayCostStrategy> {

    GatewayCostStrategy queryById(Serializable id);
}

