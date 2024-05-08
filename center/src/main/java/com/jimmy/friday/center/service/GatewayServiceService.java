package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.center.entity.GatewayService;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * (GatewayService)表服务接口
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
public interface GatewayServiceService extends IService<GatewayService> {

    Set<String> getGroupNames();

    List<GatewayService> queryList(String groupName, ServiceTypeEnum type);

    GatewayService queryById(Serializable id);

    GatewayService getGatewayService(Service service);

    List<GatewayService> queryByApplicationNameAndType(String applicationName, String type);

    GatewayService query(String applicationName, String type, String version);

    GatewayService getGatewayService(String applicationName, String type, String version, String group);
}

