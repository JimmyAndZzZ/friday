package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.gateway.ApplicationStatusEnum;
import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.center.dao.HawkEyeTopologyModuleDao;
import com.jimmy.friday.center.entity.HawkEyeTopologyModule;
import com.jimmy.friday.center.service.HawkEyeTopologyModuleService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentMap;

/**
 * (HawkEyeTopologyModule)表服务实现类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Service("hawkEyeTopologyModuleService")
public class HawkEyeTopologyModuleServiceImpl extends ServiceImpl<HawkEyeTopologyModuleDao, HawkEyeTopologyModule> implements HawkEyeTopologyModuleService {

    private final ConcurrentMap<String, HawkEyeTopologyModule> process = Maps.newConcurrentMap();

    @Override
    public HawkEyeTopologyModule get(Topology topology, ApplicationStatusEnum applicationStatusEnum) {
        if (topology == null) {
            return null;
        }

        String type = topology.getType();
        String machine = topology.getMachine();
        String applicationName = topology.getApplication();

        String key = machine + ":" + applicationName + ":" + type;

        QueryWrapper<HawkEyeTopologyModule> wrapper = new QueryWrapper<>();
        wrapper.eq("machine", machine);
        wrapper.eq("module", applicationName);
        wrapper.eq("type", type);

        HawkEyeTopologyModule application = process.get(key);
        if (application != null) {
            return application;
        }

        application = this.getOne(wrapper);
        if (application != null) {
            process.put(key, application);
            return application;
        }

        if (application == null) {
            application = new HawkEyeTopologyModule();
            application.setMachine(machine);
            application.setModule(applicationName);
            application.setType(type);
            application.setId(ShortUUID.uuid());
            application.setStatus(applicationStatusEnum.getStatus());

            HawkEyeTopologyModule put = process.putIfAbsent(key, application);
            if (put != null) {
                return put;
            }

            this.save(application);
        }

        return application;
    }
}

