package com.jimmy.friday.center.core.gateway.invoke;

import cn.hutool.core.util.IdUtil;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.message.gateway.Heartbeat;
import com.jimmy.friday.center.action.gateway.HeartbeatAction;
import com.jimmy.friday.center.core.gateway.RegisterCenter;
import com.jimmy.friday.center.core.gateway.grpc.GrpcFactory;
import com.jimmy.friday.center.core.gateway.grpc.client.GrpcClient;
import com.jimmy.friday.center.proto.GwPyReply;
import com.jimmy.friday.center.core.gateway.support.RegisterSupport;
import com.jimmy.friday.center.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class GrpcInvoke extends BaseInvoke {

    @Autowired
    private GrpcFactory grpcFactory;

    @Autowired
    private HeartbeatAction heartbeatAction;

    @Autowired
    private RegisterSupport registerSupport;

    @Override
    public boolean heartbeat(Service service) {
        Heartbeat heartbeat = heartbeatAction.heartbeat(new Heartbeat(IdUtil.getSnowflake(1, 1).nextId()), service.getApplicationId());
        if (heartbeat == null) {
            return false;
        }

        Boolean isBusy = heartbeat.getIsBusy();
        String serviceId = service.getServiceId();

        log.debug("grpc服务id:{},当前是否繁忙:{}", serviceId, isBusy);

        RegisterCenter registerCenter = registerSupport.get(this.type());
        if (registerCenter != null) {
            if (isBusy) {
                registerCenter.lockService(serviceId);
            } else {
                registerCenter.freeService(serviceId);
            }
        }

        return true;
    }

    @Override
    public String invoke(Service service, Method method, Map<String, String> args) throws Exception {
        GrpcClient grpcClient = grpcFactory.getGrpcClient(service.getName(), method.getMethodId(), service.getIpAddress(), service.getPort());
        GwPyReply gwPyReply = grpcClient.execute(service, method, args);
        return gwPyReply == null ? null : JsonUtil.toString(grpcClient.responseConvert(gwPyReply));
    }

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.GRPC;
    }
}
