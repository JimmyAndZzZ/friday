package com.jimmy.friday.framework.register;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.base.HeartbeatService;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.boot.other.ConfigConstants;
import com.jimmy.friday.framework.annotation.gateway.Api;
import com.jimmy.friday.framework.annotation.gateway.Condition;
import com.jimmy.friday.framework.condition.DubboCondition;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.util.List;

@Condition(condition = DubboCondition.class)
public class DubboRegister extends BaseRegister {

    @Autowired
    private ProtocolConfig protocolConfig;

    @Autowired
    private RegistryConfig registryConfig;

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.DUBBO;
    }

    @Override
    public void register() throws Exception {
        //注册心跳服务
        ServiceConfig<HeartbeatService> service = new ServiceConfig<>();
        service.setRegistry(registryConfig);
        service.setProtocol(protocolConfig);
        service.setVersion("1.0.0");
        service.setInterface(HeartbeatService.class);
        service.setRef(() -> true);
        // 导出服务
        service.export();
    }

    @Override
    protected Service geneService() throws Exception {
        String name = super.loadConfig(ConfigConstants.APPLICATION_NAME);
        if (StrUtil.isEmpty(name)) {
            throw new GatewayException("未配置应用名");
        }

        Service service = new Service();
        service.setIpAddress(super.getIpAddress());
        service.setName(name);
        service.setPort(protocolConfig.getPort());
        service.setType(type().toString());
        service.putAttribute(AttributeConstants.Dubbo.REGISTRY_CONFIG_ADDRESS, registryConfig.getAddress());
        service.putAttribute(AttributeConstants.Dubbo.PROVIDER_PROTOCOL_TYPE, protocolConfig.getName());
        return service;
    }

    @Override
    public void collectMethod(Class<?> clazz) throws Exception {
        DubboService annotation = AnnotationUtils.getAnnotation(clazz, DubboService.class);
        if (annotation == null) {
            return;
        }

        List<java.lang.reflect.Method> methodWithApi = this.getMethodWithApi(clazz);
        if (CollUtil.isEmpty(methodWithApi)) {
            return;
        }

        for (java.lang.reflect.Method method : methodWithApi) {
            Api api = AnnotationUtils.getAnnotation(method, Api.class);

            Class<?> interfaceFromMethod = this.getInterfaceFromMethod(clazz.getInterfaces(), method);
            if (interfaceFromMethod == null) {
                continue;
            }

            Method m = this.geneMethod(method, api);
            m.setDubboVersion(annotation.version());
            m.setInterfaceName(interfaceFromMethod.getName());
            super.fallbackHandler(clazz, api, m, method);
            this.addMethod(m);
        }
    }

    @Override
    public TypeFilter getTypeFilter() {
        return new AnnotationTypeFilter(DubboService.class);
    }
}
