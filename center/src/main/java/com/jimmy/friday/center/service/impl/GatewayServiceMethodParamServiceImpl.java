package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Param;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.dao.GatewayServiceMethodParamDao;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceMethod;
import com.jimmy.friday.center.entity.GatewayServiceMethodParam;
import com.jimmy.friday.center.service.GatewayServiceMethodParamService;
import com.jimmy.friday.center.service.GatewayServiceMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * (GatewayServiceMethodParam)表服务实现类
 *
 * @author makejava
 * @since 2024-03-26 17:55:21
 */
@Slf4j
@Service("gatewayServiceMethodParamService")
public class GatewayServiceMethodParamServiceImpl extends ServiceImpl<GatewayServiceMethodParamDao, GatewayServiceMethodParam> implements GatewayServiceMethodParamService {

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Override
    public void removeByServiceId(Long serviceId) {
        QueryWrapper<GatewayServiceMethodParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("service_id", serviceId);
        this.remove(queryWrapper);
    }

    @Override
    public List<GatewayServiceMethodParam> queryByServiceId(Long serviceId) {
        QueryWrapper<GatewayServiceMethodParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("service_id", serviceId);
        return this.list(queryWrapper);
    }

    @Override
    public List<GatewayServiceMethodParam> queryByMethodId(Long methodId) {
        QueryWrapper<GatewayServiceMethodParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("method_id", methodId);
        return this.list(queryWrapper);
    }

    @Override
    public void refreshMethod(GatewayService gatewayService, com.jimmy.friday.boot.core.gateway.Service service) {
        //grpc不处理
        if (ServiceTypeEnum.GRPC.toString().equalsIgnoreCase(service.getType())) {
            return;
        }

        Long id = gatewayService.getId();
        List<Method> methods = service.getMethods();
        Map<String, Method> map = CollUtil.isEmpty(methods) ? Maps.newHashMap() : methods.stream().collect(Collectors.toMap(Method::getMethodId, g -> g));
        //删除参数列表
        this.removeByServiceId(id);

        if (MapUtil.isEmpty(map)) {
            gatewayServiceMethodService.removeByServiceId(id);
            return;
        }

        List<GatewayServiceMethod> update = Lists.newArrayList();
        List<GatewayServiceMethodParam> methodParams = Lists.newArrayList();
        List<GatewayServiceMethod> gatewayServiceMethods = gatewayServiceMethodService.queryByServiceId(id);
        if (CollUtil.isNotEmpty(gatewayServiceMethods)) {
            for (int i = gatewayServiceMethods.size() - 1; i >= 0; i--) {
                GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethods.get(i);

                String methodId = gatewayServiceMethod.getMethodId();

                Method method = map.get(methodId);
                if (method != null) {
                    if (YesOrNoEnum.YES.getCode().equals(gatewayServiceMethod.getIsManual())) {
                        method.setRetry(gatewayServiceMethod.getRetry());
                        method.setTimeout(gatewayServiceMethod.getTimeout());
                    } else {
                        gatewayServiceMethod.setRetry(method.getRetry());
                        gatewayServiceMethod.setTimeout(method.getTimeout());
                        gatewayServiceMethod.setReturnType(method.getReturnType());
                        gatewayServiceMethod.setParamType(CollUtil.join(method.getParams().stream().map(Param::getDisplay).collect(Collectors.toList()), StrUtil.SPACE));
                        update.add(gatewayServiceMethod);
                    }

                    map.remove(methodId);
                    gatewayServiceMethods.remove(i);

                    List<Param> params = Lists.newArrayList();
                    params.addAll(method.getParams());
                    if (service.getType().equalsIgnoreCase(ServiceTypeEnum.HTTP.toString()) || service.getType().equalsIgnoreCase(ServiceTypeEnum.SPRING_CLOUD.toString())) {
                        params.addAll(method.getHttpPathParams());
                    }

                    if (CollUtil.isNotEmpty(params)) {
                        for (Param param : params) {
                            GatewayServiceMethodParam gatewayServiceMethodParam = new GatewayServiceMethodParam();
                            gatewayServiceMethodParam.setServiceId(id);
                            gatewayServiceMethodParam.setMethodId(gatewayServiceMethod.getId());
                            gatewayServiceMethodParam.setParamType(param.getType());
                            gatewayServiceMethodParam.setName(param.getName());
                            gatewayServiceMethodParam.setDesc(param.getDesc());
                            methodParams.add(gatewayServiceMethodParam);
                        }
                    }
                }
            }
        }

        if (CollUtil.isNotEmpty(gatewayServiceMethods)) {
            gatewayServiceMethodService.remove(gatewayServiceMethods);
        }

        if (MapUtil.isNotEmpty(map)) {
            for (Map.Entry<String, Method> entry : map.entrySet()) {
                String mapKey = entry.getKey();
                Method mapValue = entry.getValue();

                GatewayServiceMethod gatewayServiceMethod = new GatewayServiceMethod();
                gatewayServiceMethod.setRetry(mapValue.getRetry());
                gatewayServiceMethod.setServiceId(id);
                gatewayServiceMethod.setMethodId(mapKey);
                gatewayServiceMethod.setName(mapValue.getName());
                gatewayServiceMethod.setInterfaceName(mapValue.getInterfaceName());
                gatewayServiceMethod.setTimeout(mapValue.getTimeout());
                gatewayServiceMethod.setReturnType(mapValue.getReturnTypeDisplay());
                gatewayServiceMethod.setIsManual(YesOrNoEnum.NO.getCode());
                gatewayServiceMethod.setMethodCode(SecureUtil.md5(mapValue.key()));
                gatewayServiceMethod.setParamType(CollUtil.join(mapValue.getParams().stream().map(Param::getDisplay).collect(Collectors.toList()), StrUtil.SPACE));
                gatewayServiceMethodService.save(gatewayServiceMethod);

                List<Param> params = mapValue.getParams();
                if (CollUtil.isNotEmpty(params)) {
                    for (Param param : params) {
                        GatewayServiceMethodParam gatewayServiceMethodParam = new GatewayServiceMethodParam();
                        gatewayServiceMethodParam.setServiceId(id);
                        gatewayServiceMethodParam.setMethodId(gatewayServiceMethod.getId());
                        gatewayServiceMethodParam.setParamType(param.getType());
                        gatewayServiceMethodParam.setName(param.getName());
                        gatewayServiceMethodParam.setDesc(param.getDesc());
                        methodParams.add(gatewayServiceMethodParam);
                    }
                }
            }
        }

        if (CollUtil.isNotEmpty(update)) {
            gatewayServiceMethodService.updateBatchById(update);
        }

        if (CollUtil.isNotEmpty(methodParams)) {
            this.saveBatch(methodParams);
        }
    }
}

