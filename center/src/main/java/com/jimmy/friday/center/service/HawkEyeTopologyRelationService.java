package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.center.entity.HawkEyeTopologyRelation;

/**
 * (HawkEyeTopologyRelation)表服务接口
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
public interface HawkEyeTopologyRelationService extends IService<HawkEyeTopologyRelation> {

    void add(Topology from, Topology to, String invokeRemark, String invokeType);
}

