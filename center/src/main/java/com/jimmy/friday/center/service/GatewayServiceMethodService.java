package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.center.entity.GatewayServiceMethod;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * (GatewayServiceMethod)表服务接口
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
public interface GatewayServiceMethodService extends IService<GatewayServiceMethod> {

    Long getLastInvokeTimestamp(Long methodId);

    Map<Long, Long> getMethodIdMapperServiceId();

    Integer getTodayMethodInvokeCount(Long methodId);

    GatewayServiceMethod queryByMethod(String methodCode, String methodId, Long serviceId);

    GatewayServiceMethod queryByMethodCode(String methodCode, Long serviceId);

    GatewayServiceMethod queryByMethod(Method method, Long serviceId);

    GatewayServiceMethod queryByMethodId(String methodId, Long serviceId);

    GatewayServiceMethod query(Service service, Method method);


    GatewayServiceMethod queryById(Serializable id);

    void remove(List<GatewayServiceMethod> gatewayServiceMethods);

    void removeByServiceId(Long serviceId);

    Integer getHistoryMethodInvokeCount(Long methodId);

    List<GatewayServiceMethod> queryByServiceId(Long serviceId);

}

