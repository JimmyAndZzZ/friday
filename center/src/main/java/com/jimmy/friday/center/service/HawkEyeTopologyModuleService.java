package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.gateway.ApplicationStatusEnum;
import com.jimmy.friday.center.entity.HawkEyeTopologyModule;

/**
 * (HawkEyeTopologyModule)表服务接口
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
public interface HawkEyeTopologyModuleService extends IService<HawkEyeTopologyModule> {

    HawkEyeTopologyModule get(Topology topology, ApplicationStatusEnum applicationStatusEnum);
}

