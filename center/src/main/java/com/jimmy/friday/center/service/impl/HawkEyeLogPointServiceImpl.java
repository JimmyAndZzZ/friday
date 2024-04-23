package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.message.agent.AgentRunPoint;
import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.center.dao.HawkEyeLogPointDao;
import com.jimmy.friday.center.entity.HawkEyeLogPoint;
import com.jimmy.friday.center.service.HawkEyeLogPointService;
import org.springframework.stereotype.Service;

/**
 * (HawkEyeLogPoint)表服务实现类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Service("hawkEyeLogPointService")
public class HawkEyeLogPointServiceImpl extends ServiceImpl<HawkEyeLogPointDao, HawkEyeLogPoint> implements HawkEyeLogPointService {

    @Override
    public void createPoint(AgentRunPoint agentRunPoint) {
        HawkEyeLogPoint hawkEyeLogPoint = new HawkEyeLogPoint();
        hawkEyeLogPoint.setCreateDate(agentRunPoint.getDate());
        hawkEyeLogPoint.setId(ShortUUID.uuid());
        hawkEyeLogPoint.setApplicationName(agentRunPoint.getApplicationName());
        hawkEyeLogPoint.setClassName(agentRunPoint.getClassName());
        hawkEyeLogPoint.setMethodName(agentRunPoint.getMethodName());
        hawkEyeLogPoint.setTraceId(agentRunPoint.getTraceId());
        this.save(hawkEyeLogPoint);
    }
}

