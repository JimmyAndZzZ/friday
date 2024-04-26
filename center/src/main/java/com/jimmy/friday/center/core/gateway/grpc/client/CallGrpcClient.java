package com.jimmy.friday.center.core.gateway.grpc.client;

import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.proto.DemoGatewayServiceGrpc;
import com.jimmy.friday.center.proto.GwPyReply;
import com.jimmy.friday.center.proto.GwPyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class CallGrpcClient extends GrpcClient {

    private final DemoGatewayServiceGrpc.DemoGatewayServiceBlockingStub serviceBlockingStub;

    public CallGrpcClient(String host, int port, ApplicationContext applicationContext) {
        super(host, port, applicationContext);

        this.serviceBlockingStub = DemoGatewayServiceGrpc.newBlockingStub(this.channel);
    }

    @Override
    public GwPyReply execute(Service service, Method method, Map<String, String> args) {
        return this.grpcInvoke(service, method, args, 0);
    }

    @Override
    public void shutdown() throws InterruptedException {
        super.shutdown();
    }

    /**
     * grpc调用
     *
     * @param service
     * @param method
     * @param args
     * @param invokeCount
     * @return
     */
    private GwPyReply grpcInvoke(Service service, Method method, Map<String, String> args, int invokeCount) {
        GwPyRequest gwPyRequest = super.requestConvert(args);

        log.info("准备调用grpc,name:{},ip:{},port:{},args:{}", service.getName(), service.getIpAddress(), service.getPort(), args);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<GwPyReply> task = () -> serviceBlockingStub.call(gwPyRequest);

        Future<GwPyReply> future = executor.submit(task);

        try {
            return future.get(method.getTimeout(), TimeUnit.SECONDS); // 设置超时时间为2秒
        } catch (TimeoutException e) {
            log.error("调用grpc超时,name:{},ip:{},port:{},args:{}", service.getName(), service.getIpAddress(), service.getPort(), args);

            if (invokeCount < method.getRetry()) {
                log.info("准备进行重试,name:{},ip:{},port:{},args:{},重试次数:{}", service.getName(), service.getIpAddress(), service.getPort(), args, ++invokeCount);
                return this.grpcInvoke(service, method, args, invokeCount);
            }

            throw new GatewayException("执行超时");
        } catch (InterruptedException | ExecutionException e) {
            throw new GatewayException("执行被中断");
        }
    }
}
