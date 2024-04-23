package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayCostStrategyDao;
import com.jimmy.friday.center.entity.GatewayCostStrategy;
import com.jimmy.friday.center.service.GatewayCostStrategyService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * (GatewayCostStrategy)表服务实现类
 *
 * @author makejava
 * @since 2024-01-02 17:53:21
 */
@Service("gatewayCostStrategyService")
public class GatewayCostStrategyServiceImpl extends ServiceImpl<GatewayCostStrategyDao, GatewayCostStrategy> implements GatewayCostStrategyService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public GatewayCostStrategy getById(Serializable id) {
        return attachmentCache.attachment(RedisConstants.COST_STRATEGY_CACHE, id.toString(), GatewayCostStrategy.class, () -> super.getById(id));
    }

    @Override
    public GatewayCostStrategy queryById(Serializable id) {
        return super.getById(id);
    }
}

