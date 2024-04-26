package com.jimmy.friday.center.core.gateway.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.cron.CronUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceStatusEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.gateway.GatewayCircuitBreakerManager;
import com.jimmy.friday.center.core.gateway.RegisterCenter;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceProvider;
import com.jimmy.friday.center.service.GatewayServiceMethodParamService;
import com.jimmy.friday.center.service.GatewayServiceMethodService;
import com.jimmy.friday.center.service.GatewayServiceProviderService;
import com.jimmy.friday.center.service.GatewayServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RegisterSupport implements Initialize {

    private final ConcurrentMap<ServiceTypeEnum, RegisterCenter> registerCenterMap = Maps.newConcurrentMap();

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private InvokeSupport invokeSupport;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayCircuitBreakerManager gatewayCircuitBreakerManager;

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Autowired
    private GatewayServiceProviderService gatewayServiceProviderService;

    @Autowired
    private GatewayServiceMethodParamService gatewayServiceMethodParamService;

    public RegisterCenter get(ServiceTypeEnum serviceType) {
        RegisterCenter registerCenter = registerCenterMap.get(serviceType);
        if (registerCenter != null) {
            return registerCenter;
        }

        registerCenter = new RegisterCenter(
                applicationContext,
                serviceType,
                stripedLock,
                invokeSupport,
                attachmentCache,
                gatewayServiceProviderService,
                gatewayServiceMethodService,
                gatewayCircuitBreakerManager,
                gatewayServiceService,
                gatewayServiceMethodParamService);
        RegisterCenter put = registerCenterMap.putIfAbsent(serviceType, registerCenter);
        if (put != null) {
            registerCenter.close();
            registerCenter = null;
            return put;
        }

        return registerCenter;
    }

    @Override
    public void init() throws Exception {
        CronUtil.schedule(IdUtil.simpleUUID(), "0 */5 * * * ?", () -> {
            try {
                List<GatewayServiceProvider> list = gatewayServiceProviderService.list();
                if (CollUtil.isNotEmpty(list)) {
                    Collection<GatewayService> gatewayServices = gatewayServiceService.listByIds(list.stream().map(GatewayServiceProvider::getServiceId).collect(Collectors.toSet()));
                    Map<Long, GatewayService> serviceMap = CollUtil.isEmpty(gatewayServices) ? Maps.newHashMap() : gatewayServices.stream().collect(Collectors.toMap(GatewayService::getId, g -> g));

                    for (GatewayServiceProvider gatewayServiceProvider : list) {
                        Long id = gatewayServiceProvider.getId();
                        Long serviceId = gatewayServiceProvider.getServiceId();

                        GatewayService gatewayService = serviceMap.get(serviceId);
                        if (gatewayService == null) {
                            gatewayServiceProviderService.updateStatus(ServiceStatusEnum.DISCONNECT.getCode(), id);
                            continue;
                        }

                        ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.queryByType(gatewayService.getType());
                        if (serviceTypeEnum == null) {
                            gatewayServiceProviderService.updateStatus(ServiceStatusEnum.DISCONNECT.getCode(), id);
                            continue;
                        }

                        RegisterCenter registerCenter = registerCenterMap.get(serviceTypeEnum);
                        if (registerCenter == null) {
                            gatewayServiceProviderService.updateStatus(ServiceStatusEnum.DISCONNECT.getCode(), id);
                            continue;
                        }

                        String centerServiceId = registerCenter.getServiceId(gatewayService.getApplicationName(), gatewayServiceProvider.getIpAddress(), gatewayServiceProvider.getPort(), gatewayService.getVersion());

                        Service service = registerCenter.getByServiceId(centerServiceId);
                        if (service == null) {
                            gatewayServiceProviderService.updateStatus(ServiceStatusEnum.DISCONNECT.getCode(), id);
                        } else {
                            gatewayServiceProviderService.updateStatus(service.getStatus().getCode(), id);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("服务状态刷新失败", e);
            }
        });
    }

    @Override
    public int sort() {
        return 2;
    }
}
