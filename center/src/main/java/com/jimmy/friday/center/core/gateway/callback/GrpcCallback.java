package com.jimmy.friday.center.core.gateway.callback;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.GrpcMethodEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.base.Callback;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceMethod;
import com.jimmy.friday.center.service.GatewayServiceMethodService;
import com.jimmy.friday.center.service.GatewayServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

@Component
public class GrpcCallback implements Callback {

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Override
    public void register(Service service) {
        GatewayService gatewayService = gatewayServiceService.getGatewayService(service);

        Lock lock = stripedLock.getLocalLock("service", 8, gatewayService.getId());
        lock.lock();
        try {
            for (GrpcMethodEnum value : GrpcMethodEnum.values()) {
                QueryWrapper<GatewayServiceMethod> wrapper = new QueryWrapper<>();
                wrapper.eq("service_id", gatewayService.getId());
                wrapper.eq("name", value.getMethodName());
                GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethodService.getOne(wrapper);
                if (gatewayServiceMethod == null) {
                    gatewayServiceMethod = new GatewayServiceMethod();
                    gatewayServiceMethod.setRetry(0);
                    gatewayServiceMethod.setServiceId(gatewayService.getId());
                    gatewayServiceMethod.setMethodId(value.getMethodName());
                    gatewayServiceMethod.setName(value.getMethodName());
                    gatewayServiceMethod.setTimeout(30);
                    gatewayServiceMethod.setParamType("GwPyRequest");
                    gatewayServiceMethod.setReturnType("GwPyReply");
                    gatewayServiceMethod.setIsManual(YesOrNoEnum.NO.getCode());
                    gatewayServiceMethodService.save(gatewayServiceMethod);
                }

                Method method = new Method();
                method.setMethodId(gatewayServiceMethod.getMethodId());
                method.setRetry(gatewayServiceMethod.getRetry());
                method.setTimeout(gatewayServiceMethod.getTimeout());
                method.setName(gatewayServiceMethod.getName());
                method.setIsSync(value.getIsSync());
                service.getMethods().add(method);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.GRPC;
    }
}
