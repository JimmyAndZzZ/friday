package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.core.agent.Qps;
import com.jimmy.friday.boot.enums.ApplicationStatusEnum;
import com.jimmy.friday.boot.message.agent.AgentQps;
import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.center.annotation.Async;
import com.jimmy.friday.center.dao.HawkEyeQpsDao;
import com.jimmy.friday.center.entity.HawkEyeQps;
import com.jimmy.friday.center.entity.HawkEyeTopologyModule;
import com.jimmy.friday.center.service.HawkEyeQpsService;
import com.jimmy.friday.center.service.HawkEyeTopologyModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (HawkEyeQps)表服务实现类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Service("hawkEyeQpsService")
public class HawkEyeQpsServiceImpl extends ServiceImpl<HawkEyeQpsDao, HawkEyeQps> implements HawkEyeQpsService {

    @Autowired
    private HawkEyeTopologyModuleService hawkEyeTopologyModuleService;

    @Override
    @Async(topic = "hawk_eye_qps_topic", groupId = "hawk_eye_qps")
    public void save(AgentQps agentQps) {
        HawkEyeTopologyModule hawkEyeTopologyModule = hawkEyeTopologyModuleService.get(agentQps.getServer(), ApplicationStatusEnum.OPEN);

        List<Qps> qpsList = agentQps.getQpsList();
        if (CollUtil.isNotEmpty(qpsList)) {
            this.saveBatch(qpsList.stream().map(qps -> {
                HawkEyeQps hawkEyeQps = new HawkEyeQps();
                hawkEyeQps.setId(ShortUUID.uuid());
                hawkEyeQps.setModuleName(hawkEyeTopologyModule.getModule());
                hawkEyeQps.setCreateDate(qps.getCreateDate());
                hawkEyeQps.setRequestAttachment(qps.getRequestAttachment());
                hawkEyeQps.setRequestPoint(qps.getRequestPoint());
                hawkEyeQps.setProtocol(qps.getProtocol());
                return hawkEyeQps;
            }).collect(Collectors.toList()));
        }
    }
}

