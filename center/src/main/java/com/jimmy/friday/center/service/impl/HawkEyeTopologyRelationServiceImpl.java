package com.jimmy.friday.center.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.gateway.ApplicationStatusEnum;
import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.center.dao.HawkEyeTopologyRelationDao;
import com.jimmy.friday.center.entity.HawkEyeTopologyModule;
import com.jimmy.friday.center.entity.HawkEyeTopologyRelation;
import com.jimmy.friday.center.service.HawkEyeTopologyModuleService;
import com.jimmy.friday.center.service.HawkEyeTopologyRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * (HawkEyeTopologyRelation)表服务实现类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Service("hawkEyeTopologyRelationService")
public class HawkEyeTopologyRelationServiceImpl extends ServiceImpl<HawkEyeTopologyRelationDao, HawkEyeTopologyRelation> implements HawkEyeTopologyRelationService {

    @Autowired
    private HawkEyeTopologyModuleService hawkEyeTopologyModuleService;

    @Override
    public void add(Topology from, Topology to, String invokeRemark, String invokeType) {
        HawkEyeTopologyModule fromApplication = hawkEyeTopologyModuleService.get(from, ApplicationStatusEnum.OPEN);
        HawkEyeTopologyModule toApplication = hawkEyeTopologyModuleService.get(to, ApplicationStatusEnum.OPEN);

        if (from != null && to != null) {
            if (fromApplication.getId().equalsIgnoreCase(toApplication.getId())) {
                return;
            }

            QueryWrapper<HawkEyeTopologyRelation> wrapper = new QueryWrapper<>();
            wrapper.eq("invoke_remark", invokeRemark);
            wrapper.eq("invoke_type", invokeType);
            wrapper.eq("up", fromApplication.getId());

            HawkEyeTopologyRelation hawkEyeTopologyRelation = this.getOne(wrapper);
            if (hawkEyeTopologyRelation == null) {
                hawkEyeTopologyRelation = new HawkEyeTopologyRelation();
                hawkEyeTopologyRelation.setUp(fromApplication.getId());
                hawkEyeTopologyRelation.setDown(toApplication.getId());
                hawkEyeTopologyRelation.setId(ShortUUID.uuid());
                hawkEyeTopologyRelation.setInvokeRemark(invokeRemark);
                hawkEyeTopologyRelation.setInvokeType(invokeType);
                this.save(hawkEyeTopologyRelation);
            } else {
                String down = hawkEyeTopologyRelation.getDown();
                if (StrUtil.isEmpty(down)) {
                    hawkEyeTopologyRelation.setDown(toApplication.getId());

                    QueryWrapper<HawkEyeTopologyRelation> update = new QueryWrapper<>();
                    update.eq("id", hawkEyeTopologyRelation.getId());
                    this.update(hawkEyeTopologyRelation, update);
                }
            }
        }

        if (from != null && to == null) {
            QueryWrapper<HawkEyeTopologyRelation> wrapper = new QueryWrapper<>();
            wrapper.eq("invoke_remark", invokeRemark);
            wrapper.eq("invoke_type", invokeType);
            wrapper.eq("up", fromApplication.getId());
            HawkEyeTopologyRelation hawkEyeTopologyRelation = this.getOne(wrapper);
            if (hawkEyeTopologyRelation == null) {
                hawkEyeTopologyRelation = new HawkEyeTopologyRelation();
                hawkEyeTopologyRelation.setUp(fromApplication.getId());
                hawkEyeTopologyRelation.setId(ShortUUID.uuid());
                hawkEyeTopologyRelation.setInvokeRemark(invokeRemark);
                hawkEyeTopologyRelation.setInvokeType(invokeType);
                this.save(hawkEyeTopologyRelation);
            }
        }

        if (from == null && to != null) {
            QueryWrapper<HawkEyeTopologyRelation> wrapper = new QueryWrapper<>();
            wrapper.eq("invoke_remark", invokeRemark);
            wrapper.eq("invoke_type", invokeType);
            wrapper.eq("down", toApplication.getId());
            HawkEyeTopologyRelation hawkEyeTopologyRelation = this.getOne(wrapper);
            if (hawkEyeTopologyRelation == null) {
                hawkEyeTopologyRelation = new HawkEyeTopologyRelation();
                hawkEyeTopologyRelation.setDown(toApplication.getId());
                hawkEyeTopologyRelation.setId(ShortUUID.uuid());
                hawkEyeTopologyRelation.setInvokeRemark(invokeRemark);
                hawkEyeTopologyRelation.setInvokeType(invokeType);
                this.save(hawkEyeTopologyRelation);
            }
        }
    }
}

