package com.jimmy.friday.center.grpc.client;

import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.NotifyTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.center.core.GatewaySession;
import com.jimmy.friday.center.event.NotifyEvent;
import com.jimmy.friday.center.grpc.GrpcErrorMapper;
import com.jimmy.friday.center.proto.DemoGatewayServiceGrpc;
import com.jimmy.friday.center.proto.GwPyReply;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

@Slf4j
public class AsyncCallGrpcClient extends GrpcClient {

    private final DemoGatewayServiceGrpc.DemoGatewayServiceStub serviceBlockingStub;

    public AsyncCallGrpcClient(String host, int port, ApplicationContext applicationContext) {
        super(host, port, applicationContext);

        this.serviceBlockingStub = DemoGatewayServiceGrpc.newStub(this.channel);
    }

    @Override
    public GwPyReply execute(Service service, Method method, Map<String, String> args) {
        log.info("准备异步调用grpc,name:{},ip:{},port:{},args:{}", service.getName(), service.getIpAddress(), service.getPort(), args);

        Long traceId = GatewaySession.getTraceId();
        String serviceId = GatewaySession.getServiceId();

        serviceBlockingStub.asyncCall(super.requestConvert(args), new StreamObserver<GwPyReply>() {
            @Override
            public void onNext(GwPyReply gwPyReply) {
                GwPyReply.STS sts = gwPyReply.getSts();
                if (sts == null) {
                    log.error("返回类型为空");
                    return;
                }

                log.info("接收到回调,sts:{},", sts);

                NotifyEvent notifyEvent = new NotifyEvent(applicationContext);
                notifyEvent.setTraceId(traceId);
                notifyEvent.setServiceId(serviceId);
                notifyEvent.setServiceTypeEnum(ServiceTypeEnum.GRPC);

                switch (sts) {
                    case PROGRESS:
                        notifyEvent.setNotifyType(NotifyTypeEnum.PROGRESS);
                        notifyEvent.setProgressRate(gwPyReply.getProgressRate());
                        applicationContext.publishEvent(notifyEvent);
                        break;
                    case FAIL:
                        notifyEvent.setNotifyType(NotifyTypeEnum.ERROR);
                        notifyEvent.setErrorMessage(GrpcErrorMapper.errorCodeMapper(String.valueOf(gwPyReply.getRet())));
                        applicationContext.publishEvent(notifyEvent);
                        break;
                    case FINISH:
                        notifyEvent.setNotifyType(NotifyTypeEnum.COMPLETED);
                        notifyEvent.setResponse(responseConvert(gwPyReply));
                        applicationContext.publishEvent(notifyEvent);
                        break;
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });

        return null;
    }
}
