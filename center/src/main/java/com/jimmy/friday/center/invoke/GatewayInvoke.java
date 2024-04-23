package com.jimmy.friday.center.invoke;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.Heartbeat;
import com.jimmy.friday.center.action.gateway.HeartbeatAction;
import com.jimmy.friday.center.core.GatewayInvokeFuture;
import com.jimmy.friday.center.core.GatewaySession;
import com.jimmy.friday.center.event.SuspectedFailEvent;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GatewayInvoke extends BaseInvoke {

    @Autowired
    private HeartbeatAction heartbeatAction;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public boolean heartbeat(Service service) {
        Heartbeat heartbeat = heartbeatAction.heartbeat(new Heartbeat(IdUtil.getSnowflake(1, 1).nextId()), service.getApplicationId());
        return heartbeat != null;
    }

    @Override
    public String invoke(Service service, Method method, Map<String, String> args) throws Exception {
        Integer retry = method.getRetry();
        String applicationId = service.getApplicationId();

        log.info("准备调用gateway,name:{},ip:{},port:{},method:{},args:{}", service.getName(), service.getIpAddress(), service.getPort(), method.getName(), args);

        Channel channel = ChannelHandlerPool.getChannel(applicationId);
        if (channel == null) {
            SuspectedFailEvent suspectedFailEvent = new SuspectedFailEvent(applicationContext);
            suspectedFailEvent.setService(service);
            applicationContext.publishEvent(suspectedFailEvent);

            throw new GatewayException("provider不存在");
        }

        com.jimmy.friday.boot.message.gateway.GatewayInvoke message = new com.jimmy.friday.boot.message.gateway.GatewayInvoke();
        message.setInvokeInterface(method.getInterfaceName());
        message.setInvokeMethod(method.getName());
        message.setIsFallback(GatewaySession.getFallback());
        message.setApplicationId(service.getApplicationId());
        message.setTraceId(IdUtil.getSnowflake(1, 1).nextId());

        List<Param> params = method.getParams();
        if (CollUtil.isNotEmpty(params)) {
            if (MapUtil.isEmpty(args)) {
                throw new GatewayException("参数为空");
            }

            for (Param param : params) {
                String name = param.getName();
                message.addInvokeParam(name, param.getType(), args.get(name));
            }
        }

        int i = 0;
        while (true) {
            com.jimmy.friday.boot.message.gateway.GatewayInvoke response = new GatewayInvokeFuture(message, channel, method.getTimeout()).get();

            if (i >= retry) {
                return this.responseHandler(response);
            }

            i++;

            if (response.getIsSuccess()) {
                return response.getJsonResult();
            }

            if (response.getIsNeedRetry()) {
                continue;
            }

            return this.responseHandler(response);
        }
    }

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.GATEWAY;
    }

    /**
     * 响应处理
     *
     * @param response
     * @return
     */
    private String responseHandler(com.jimmy.friday.boot.message.gateway.GatewayInvoke response) throws Exception {
        Boolean isSuccess = response.getIsSuccess();
        String jsonResult = response.getJsonResult();

        if (isSuccess) {
            return jsonResult;
        }

        throw super.geneException(response.getError(), response.getExceptionClass());
    }

}
