package com.jimmy.friday.center.core.gateway.grpc;

import com.google.common.collect.Maps;
import com.jimmy.friday.center.core.gateway.grpc.client.GrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class GrpcConnector {

    private final ConcurrentMap<String, GrpcClient> clientMap = Maps.newConcurrentMap();

    private final Class<? extends GrpcClient> clazz;

    private final ApplicationContext applicationContext;

    public GrpcConnector(ApplicationContext applicationContext, Class<? extends GrpcClient> clazz) {
        this.clazz = clazz;
        this.applicationContext = applicationContext;
    }

    public GrpcClient getGrpcClient(String ip, Integer port) throws Exception {
        String key = ip + ":" + port;
        GrpcClient grpcClient = clientMap.get(key);
        if (grpcClient != null) {
            return grpcClient;
        }

        Constructor<? extends GrpcClient> constructor = clazz.getConstructor(String.class, int.class, ApplicationContext.class);
        grpcClient = constructor.newInstance(ip, port, applicationContext);

        GrpcClient put = clientMap.putIfAbsent(key, grpcClient);
        if (put != null) {
            grpcClient.shutdown();
            return put;
        }

        clientMap.put(key, grpcClient);
        return grpcClient;
    }
}
