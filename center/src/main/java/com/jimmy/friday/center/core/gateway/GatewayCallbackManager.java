package com.jimmy.friday.center.core.gateway;

import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.gateway.NotifyTypeEnum;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.boot.message.gateway.InvokeCallback;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.event.NotifyEvent;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.core.gateway.support.RegisterSupport;
import com.jimmy.friday.center.utils.JsonUtil;
import com.jimmy.friday.center.utils.RedisConstants;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GatewayCallbackManager implements ApplicationListener<NotifyEvent> {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private RegisterSupport registerSupport;

    public void registerCallback(Long traceId, String applicationId) {
        log.info("注册回调:traceId:{},applicationId:{}", traceId, applicationId);

        attachmentCache.attachString(RedisConstants.Gateway.GATEWAY_INVOKE_CALLBACK + traceId, applicationId);
    }

    public void cancelCallback(Long traceId) {
        attachmentCache.remove(RedisConstants.Gateway.GATEWAY_INVOKE_CALLBACK + traceId);
    }

    @Override
    public void onApplicationEvent(NotifyEvent event) {
        try {
            Long traceId = event.getTraceId();
            String serviceId = event.getServiceId();
            NotifyTypeEnum notifyType = event.getNotifyType();
            ServiceTypeEnum serviceTypeEnum = event.getServiceTypeEnum();

            String applicationId = attachmentCache.attachment(RedisConstants.Gateway.GATEWAY_INVOKE_CALLBACK + traceId);
            if (StrUtil.isEmpty(applicationId)) {
                log.error("{}回调应用id为空", traceId);
                return;
            }

            Channel c = ChannelHandlerPool.getChannel(applicationId);
            if (c == null) {
                log.error("当前应用id不存在:{},traceId:{}", applicationId, traceId);
                return;
            }

            InvokeCallback invokeCallback = new InvokeCallback();
            invokeCallback.setTraceId(traceId);
            invokeCallback.setNotifyType(notifyType);

            switch (notifyType) {
                case PROGRESS:
                    invokeCallback.setProgressRate(event.getProgressRate());
                    break;
                case TIME_OUT:
                case CANCEL:
                    this.close(traceId, serviceId, serviceTypeEnum);
                    break;
                case ERROR:
                    this.close(traceId, serviceId, serviceTypeEnum);
                    invokeCallback.setErrorMessage(event.getErrorMessage());
                    break;
                case COMPLETED:
                    this.close(traceId, serviceId, serviceTypeEnum);
                    invokeCallback.setResponse(event.getResponse());
                    break;
            }

            c.writeAndFlush(new Event(EventTypeEnum.INVOKE_CALLBACK, JsonUtil.toString(invokeCallback)));
        } catch (Exception e) {
            log.error("服务异步调用回调失败", e);
        }
    }


    /**
     * 关闭回调
     *
     * @param traceId
     */
    private void close(Long traceId, String serviceId, ServiceTypeEnum serviceTypeEnum) {
        RegisterCenter registerCenter = registerSupport.get(serviceTypeEnum);
        if (registerCenter != null) {
            registerCenter.releaseService(serviceId);
        }

        this.cancelCallback(traceId);
    }
}
