package com.jimmy.friday.center.core.gateway.grpc;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.gateway.GrpcMethodEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.core.gateway.grpc.client.AsyncCallGrpcClient;
import com.jimmy.friday.center.core.gateway.grpc.client.CallGrpcClient;
import com.jimmy.friday.center.core.gateway.grpc.client.GrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class GrpcFactory {

    private final Map<String, Class<? extends GrpcClient>> classMap = Maps.newHashMap();

    private final ConcurrentMap<String, GrpcConnector> connectorMap = Maps.newConcurrentMap();

    @Autowired
    private ApplicationContext applicationContext;

    public GrpcFactory() {
        classMap.put(GrpcMethodEnum.CALL.getMethodName(), CallGrpcClient.class);
        classMap.put(GrpcMethodEnum.ASYNC_CALL.getMethodName(), AsyncCallGrpcClient.class);
    }

    public GrpcClient getGrpcClient(String serviceName, String methodId, String ip, int port) {
        try {
            String key = serviceName + ":" + methodId;

            GrpcConnector grpcConnector = connectorMap.get(key);
            if (grpcConnector != null) {
                return grpcConnector.getGrpcClient(ip, port);
            }

            Class<? extends GrpcClient> clazz = classMap.get(methodId);

            if (clazz == null) {
                throw new GatewayException(serviceName + "该grpc服务不存在");
            }

            grpcConnector = new GrpcConnector(applicationContext, clazz);
            GrpcConnector put = connectorMap.putIfAbsent(key, grpcConnector);
            return put != null ? put.getGrpcClient(ip, port) : grpcConnector.getGrpcClient(ip, port);
        } catch (Exception e) {
            log.error("获取GrpcClient失败", e);
            throw new GatewayException("获取GrpcClient失败");
        }
    }
}
