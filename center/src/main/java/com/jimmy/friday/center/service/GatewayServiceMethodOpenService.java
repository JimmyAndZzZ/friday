package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayServiceMethodOpen;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * (GatewayServiceMethodOpen)表服务接口
 *
 * @author makejava
 * @since 2024-01-03 14:44:07
 */
public interface GatewayServiceMethodOpenService extends IService<GatewayServiceMethodOpen> {

    GatewayServiceMethodOpen getByCode(String code);

    GatewayServiceMethodOpen queryByMethodId(Long methodId);

    Set<Long> queryOpenMethodIds(Collection<Long> methodIds);

    List<GatewayServiceMethodOpen> queryList();

    GatewayServiceMethodOpen queryByCode(String code);
}

