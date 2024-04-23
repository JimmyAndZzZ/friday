package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.core.agent.RunTopology;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.ApplicationStatusEnum;
import com.jimmy.friday.boot.message.agent.AgentRunTopology;
import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.center.annotation.Async;
import com.jimmy.friday.center.dao.HawkEyeLogTopologyRelationDao;
import com.jimmy.friday.center.entity.HawkEyeLogTopologyRelation;
import com.jimmy.friday.center.entity.HawkEyeTopologyModule;
import com.jimmy.friday.center.service.HawkEyeLogTopologyRelationService;
import com.jimmy.friday.center.service.HawkEyeTopologyModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (HawkEyeLogTopologyRelation)表服务实现类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Service("hawkEyeLogTopologyRelationService")
public class HawkEyeLogTopologyRelationServiceImpl extends ServiceImpl<HawkEyeLogTopologyRelationDao, HawkEyeLogTopologyRelation> implements HawkEyeLogTopologyRelationService {

    private final Cache<String, Integer> process = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES) // 设置过期时间为10分钟
            .build();

    @Autowired
    private HawkEyeTopologyModuleService hawkEyeTopologyModuleService;

    @Override
    @Async(topic = "hawk_eye_log_topology_topic", groupId = "hawk_eye_log_topology")
    public void save(AgentRunTopology agentRunTopology) {
        List<RunTopology> runTopologyList = agentRunTopology.getRunTopologyList();
        List<HawkEyeLogTopologyRelation> hawkEyeLogTopologyRelations = Lists.newArrayList();

        if (CollUtil.isEmpty(runTopologyList)) {
            return;
        }

        for (RunTopology bean : runTopologyList) {
            Topology to = bean.getTo();
            Topology from = bean.getFrom();

            HawkEyeTopologyModule fromApplication = hawkEyeTopologyModuleService.get(from, ApplicationStatusEnum.OPEN);
            HawkEyeTopologyModule toApplication = hawkEyeTopologyModuleService.get(to, ApplicationStatusEnum.OPEN);

            String toId = toApplication.getId();
            String fromId = fromApplication.getId();
            String traceId = bean.getTraceId();
            String invokeType = bean.getInvokeType();
            String invokeRemark = bean.getInvokeRemark();

            if (this.isRepeat(fromId, toId, traceId)) {
                continue;
            }

            HawkEyeLogTopologyRelation hawkEyeLogTopologyRelation = new HawkEyeLogTopologyRelation();
            hawkEyeLogTopologyRelation.setId(ShortUUID.uuid());
            hawkEyeLogTopologyRelation.setDown(toId);
            hawkEyeLogTopologyRelation.setTraceId(traceId);
            hawkEyeLogTopologyRelation.setInvokeType(invokeType);
            hawkEyeLogTopologyRelation.setInvokeRemark(invokeRemark);
            hawkEyeLogTopologyRelation.setUp(fromId);
            hawkEyeLogTopologyRelation.setCreateDate(bean.getDate());
            hawkEyeLogTopologyRelations.add(hawkEyeLogTopologyRelation);
        }

        if (CollUtil.isNotEmpty(hawkEyeLogTopologyRelations)) {
            this.saveBatch(hawkEyeLogTopologyRelations);
        }
    }

    /**
     * 判断是否重复
     *
     * @param fromId
     * @param toId
     * @param traceId
     * @return
     */
    private boolean isRepeat(String fromId, String toId, String traceId) {
        String key = traceId + ":" + fromId + ":" + toId + ":";
        if (process.getIfPresent(key) != null) {
            return true;
        }

        process.put(key, 1);

        QueryWrapper<HawkEyeLogTopologyRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("up", fromId);
        wrapper.eq("down", toId);
        wrapper.eq("trace_id", traceId);
        return this.count(wrapper) > 0;
    }
}

