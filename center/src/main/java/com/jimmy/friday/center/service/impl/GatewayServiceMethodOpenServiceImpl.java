package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayServiceMethodOpenDao;
import com.jimmy.friday.center.entity.GatewayServiceMethodOpen;
import com.jimmy.friday.center.service.GatewayServiceMethodOpenService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (GatewayServiceMethodOpen)表服务实现类
 *
 * @author makejava
 * @since 2024-01-03 14:44:07
 */
@Service("gatewayServiceMethodOpenService")
public class GatewayServiceMethodOpenServiceImpl extends ServiceImpl<GatewayServiceMethodOpenDao, GatewayServiceMethodOpen> implements GatewayServiceMethodOpenService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public GatewayServiceMethodOpen queryByCode(String code) {
        QueryWrapper<GatewayServiceMethodOpen> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        return getOne(queryWrapper);
    }

    @Override
    public GatewayServiceMethodOpen getByCode(String code) {
        return attachmentCache.attachment(RedisConstants.METHOD_OPEN_CACHE, code, GatewayServiceMethodOpen.class, () -> {
            QueryWrapper<GatewayServiceMethodOpen> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", code);
            return getOne(queryWrapper);
        });
    }

    @Override
    public List<GatewayServiceMethodOpen> queryList() {
        QueryWrapper<GatewayServiceMethodOpen> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", YesOrNoEnum.YES.getCode());
        return this.list(queryWrapper);
    }

    @Override
    public Set<Long> queryOpenMethodIds(Collection<Long> methodIds) {
        QueryWrapper<GatewayServiceMethodOpen> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("method_id");
        queryWrapper.in("method_id", methodIds);
        List<GatewayServiceMethodOpen> list = this.list(queryWrapper);
        return CollUtil.isNotEmpty(list) ? list.stream().map(GatewayServiceMethodOpen::getMethodId).collect(Collectors.toSet()) : Sets.newHashSet();
    }

    @Override
    public GatewayServiceMethodOpen queryByMethodId(Long methodId) {
        QueryWrapper<GatewayServiceMethodOpen> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("method_id", methodId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean save(GatewayServiceMethodOpen gatewayServiceMethodOpen) {
        boolean save = super.save(gatewayServiceMethodOpen);
        if (save) {
            attachmentCache.attach(RedisConstants.METHOD_OPEN_CACHE, gatewayServiceMethodOpen.getCode(), gatewayServiceMethodOpen);
        }

        return save;
    }

    @Override
    public boolean updateById(GatewayServiceMethodOpen gatewayServiceMethodOpen) {
        boolean update = super.updateById(gatewayServiceMethodOpen);
        if (update) {
            attachmentCache.attach(RedisConstants.METHOD_OPEN_CACHE, gatewayServiceMethodOpen.getCode(), gatewayServiceMethodOpen);
        }

        return update;
    }

    @Override
    public boolean removeById(Serializable id) {
        GatewayServiceMethodOpen byId = this.getById(id);
        if (byId != null) {
            boolean b = super.removeById(id);
            attachmentCache.remove(RedisConstants.METHOD_OPEN_CACHE, byId.getCode());
            return b;
        }

        return false;
    }
}

