package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayServiceDao;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.service.GatewayServiceService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (GatewayService)表服务实现类
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
@Service("gatewayServiceService")
public class GatewayServiceServiceImpl extends ServiceImpl<GatewayServiceDao, GatewayService> implements GatewayServiceService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public Set<String> getGroupNames() {
        QueryWrapper<GatewayService> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("group_name");
        queryWrapper.select("DISTINCT group_name");
        List<GatewayService> list = this.list(queryWrapper);
        return CollUtil.isEmpty(list) ? Sets.newHashSet() : list.stream().map(GatewayService::getGroupName).filter(s -> StrUtil.isNotEmpty(s)).collect(Collectors.toSet());
    }

    @Override
    public List<GatewayService> queryList(String groupName, ServiceTypeEnum type) {
        QueryWrapper<GatewayService> queryWrapper = new QueryWrapper<>();

        if (StrUtil.isNotEmpty(groupName)) {
            queryWrapper.eq("group_name", groupName);
        }

        if (type != null) {
            queryWrapper.eq("type", type.toString());
        }

        queryWrapper.orderByDesc("create_date");
        return this.list(queryWrapper);
    }

    @Override
    public List<GatewayService> queryByApplicationNameAndType(String applicationName, String type) {
        QueryWrapper<GatewayService> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_name", applicationName);
        queryWrapper.eq("type", type);
        queryWrapper.orderByDesc("create_date");
        return this.list(queryWrapper);
    }

    @Override
    public GatewayService getGatewayService(com.jimmy.friday.boot.core.gateway.Service service) {
        return this.getGatewayService(service.getName(), service.getType(), service.getVersion(), service.getGroup());
    }

    @Override
    public GatewayService getById(Serializable id) {
        return attachmentCache.attachment(RedisConstants.GATEWAY_SERVICE_CACHE, id.toString(), GatewayService.class, () -> super.getById(id));
    }

    @Override
    public GatewayService queryById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public GatewayService getGatewayService(String applicationName, String type, String version, String group) {
        String key = StrUtil.builder().append(applicationName).append(":").append(type).append(":").append(version).toString();

        GatewayService gatewayService = this.query(applicationName, type, version);
        if (gatewayService != null) {
            return gatewayService;
        }

        gatewayService = new GatewayService();
        gatewayService.setType(type);
        gatewayService.setApplicationName(applicationName);
        gatewayService.setVersion(version);
        gatewayService.setGroupName(group);
        gatewayService.setCreateDate(new Date());

        boolean b = attachmentCache.putIfAbsent(RedisConstants.GATEWAY_SERVICE_ID_MAPPER, key, YesOrNoEnum.YES.getCode());
        if (b) {
            this.save(gatewayService);
            attachmentCache.mapper(RedisConstants.GATEWAY_SERVICE_ID_MAPPER, key, gatewayService.getId());
        }

        return gatewayService;
    }

    @Override
    public GatewayService query(String applicationName, String type, String version) {
        String key = StrUtil.builder().append(applicationName).append(":").append(type).append(":").append(version).toString();

        Object id = attachmentCache.attachment(RedisConstants.GATEWAY_SERVICE_ID_MAPPER, key);
        if (id == null || id.toString().equals(YesOrNoEnum.YES.getCode())) {
            QueryWrapper<GatewayService> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("application_name", applicationName);
            queryWrapper.eq("type", type);
            queryWrapper.eq("version", version);
            GatewayService one = this.getOne(queryWrapper);

            if (one == null) {
                return null;
            }

            attachmentCache.mapper(RedisConstants.GATEWAY_SERVICE_ID_MAPPER, key, one.getId());
            return one;
        }

        return this.getById(id.toString());
    }
}

