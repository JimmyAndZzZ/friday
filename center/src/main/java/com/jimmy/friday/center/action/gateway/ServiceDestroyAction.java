package com.jimmy.friday.center.action.gateway;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ServiceDestroy;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.gateway.GatewayInvokeFuture;
import com.jimmy.friday.center.core.gateway.support.RegisterSupport;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ServiceDestroyAction implements Action<ServiceDestroy> {

    @Autowired
    private RegisterSupport registerSupport;

    @Override
    public void action(ServiceDestroy serviceDestroy, ChannelHandlerContext channelHandlerContext) {
        String id = serviceDestroy.getId();

        List<Service> services = serviceDestroy.getServices();
        if (CollUtil.isEmpty(services)) {
            return;
        }

        for (Service service : services) {
            registerSupport.get(service.serviceType()).remove(service, true, true);
        }

        Map<Long, String> serviceIdAndTraceId = GatewayInvokeFuture.getServiceIdAndTraceId();
        if (MapUtil.isNotEmpty(serviceIdAndTraceId)) {
            //遍历map
            Iterator<Map.Entry<Long, String>> entries = serviceIdAndTraceId.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Long, String> entry = entries.next();
                Long key = entry.getKey();
                String value = entry.getValue();

                if (value.equals(id)) {
                    CompletableFuture andClear = GatewayInvokeFuture.getAndClear(key);
                    if (andClear == null) {
                        continue;
                    }

                    andClear.completeExceptionally(new InterruptedException("服务掉线"));
                    entries.remove();
                }
            }
        }
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SERVICE_DESTROY;
    }
}
