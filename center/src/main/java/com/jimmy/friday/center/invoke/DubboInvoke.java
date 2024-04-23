package com.jimmy.friday.center.invoke;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.base.HeartbeatService;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.center.core.GatewaySession;
import com.jimmy.friday.center.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DubboInvoke extends BaseInvoke {

    private final Map<String, GenericService> genericServiceCache = Maps.newHashMap();

    @Override
    public boolean heartbeat(Service service) {
        try {
            String protocol = StrUtil.emptyToDefault(service.getStringAttribute(AttributeConstants.Dubbo.PROVIDER_PROTOCOL_TYPE), "dubbo");
            String url = new StringBuilder(protocol).append("://").append(service.getIpAddress()).append(":").append(service.getPort()).append("/").append(HeartbeatService.class.getName()).toString();

            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(service.getStringAttribute(AttributeConstants.Dubbo.REGISTRY_CONFIG_ADDRESS));

            ReferenceConfig<HeartbeatService> referenceConfig = new ReferenceConfig<>();
            referenceConfig.setRegistry(registryConfig);
            referenceConfig.setInterface(HeartbeatService.class);
            referenceConfig.setVersion("1.0.0");
            referenceConfig.setTimeout(GlobalConstants.DEFAULT_TIMEOUT * 1000);
            referenceConfig.setUrl(url);
            referenceConfig.setRetries(0);

            HeartbeatService heartbeatService = referenceConfig.get();
            return heartbeatService.heartbeat();
        } catch (Exception e) {
            log.error("心跳调用失败", e);
            return false;
        }
    }

    @Override
    public String invoke(Service service, Method method, Map<String, String> args) throws Exception {
        try {
            log.info("准备调用dubbo,name:{},method:{},args:{}", service.getName(), method.getName(), args);

            Boolean isRoute = GatewaySession.getIsRoute();
            String protocol = StrUtil.emptyToDefault(service.getStringAttribute(AttributeConstants.Dubbo.PROVIDER_PROTOCOL_TYPE), "dubbo");

            String interfaceName = method.getInterfaceName();
            GenericService genericService = this.genericServiceCache.get(interfaceName);
            if (genericService == null) {
                RegistryConfig registryConfig = new RegistryConfig();
                registryConfig.setAddress(service.getStringAttribute(AttributeConstants.Dubbo.REGISTRY_CONFIG_ADDRESS));

                ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
                referenceConfig.setRegistry(registryConfig);
                referenceConfig.setInterface(interfaceName);
                referenceConfig.setVersion(method.getDubboVersion());
                referenceConfig.setTimeout(method.getTimeout() * 1000);
                referenceConfig.setRetries(method.getRetry());
                referenceConfig.setProtocol(protocol);
                referenceConfig.setGeneric(Boolean.TRUE.toString());
                referenceConfig.setCheck(false);

                if (isRoute) {
                    log.info("路由准备调用dubbo,name:{},ip:{},port:{},method:{},args:{}", service.getName(), service.getIpAddress(), service.getPort(), method.getName(), args);

                    String url = new StringBuilder(protocol).append("://").append(service.getIpAddress()).append(":").append(service.getPort()).append("/").append(interfaceName).toString();
                    referenceConfig.setUrl(url);
                }

                genericService = referenceConfig.get();
                this.genericServiceCache.put(interfaceName, genericService);
            }

            ArrayList<Object> array = Lists.newArrayList();
            ArrayList<String> types = Lists.newArrayList();
            if (MapUtil.isNotEmpty(args)) {
                List<Param> params = method.getParams();

                for (Param param : params) {
                    String value = args.get(param.getName());
                    if (StrUtil.isNotEmpty(value)) {
                        String paramType = param.getType();
                        JSONValidator.Type type = JSONValidator.from(value).getType();

                        types.add(paramType);

                        switch (type) {
                            case Array:
                                array.add(JSON.parseArray(value));
                                break;
                            case Value:
                                array.add(JSON.parseObject(value, this.getClass(paramType)));
                                break;
                            case Object:
                                array.add(JSON.parseObject(value));
                                break;
                        }
                    } else {
                        array.add(null);
                    }
                }
            }

            Object result = genericService.$invoke(method.getName(), ArrayUtil.toArray(types, String.class), array.toArray());
            return result != null ? JsonUtil.toString(result) : null;
        } catch (Exception e) {
            log.error("dubbo调用失败", e);
            throw new GatewayException(e.getMessage());
        }
    }

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.DUBBO;
    }

    /**
     * 获取参数类
     *
     * @param paramType
     * @return
     * @throws Exception
     */
    private Class<?> getClass(String paramType) throws Exception {
        switch (paramType) {
            case "int":
                return Integer.TYPE;
            case "byte":
                return Byte.TYPE;
            case "short":
                return Short.TYPE;
            case "long":
                return Long.TYPE;
            case "float":
                return Float.TYPE;
            case "double":
                return Double.TYPE;
            case "char":
                return Character.TYPE;
            case "boolean":
                return Boolean.TYPE;
            default:
                return Class.forName(paramType);
        }
    }
}
