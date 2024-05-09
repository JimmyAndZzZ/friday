package com.jimmy.friday.framework.register;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.framework.annotation.gateway.Api;
import com.jimmy.friday.framework.annotation.gateway.Condition;
import com.jimmy.friday.framework.annotation.gateway.GatewayService;
import com.jimmy.friday.framework.condition.GatewayCondition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.util.List;

@Condition(condition = GatewayCondition.class)
public class GatewayRegister extends BaseRegister {

    @Override
    public ServiceTypeEnum type() {
        return ServiceTypeEnum.GATEWAY;
    }

    @Override
    public void register() throws Exception {

    }

    @Override
    public void collectMethod(Class<?> clazz) throws Exception {
        GatewayService annotation = AnnotationUtils.getAnnotation(clazz, GatewayService.class);
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

            invokeSupport.putInvokeInterfaceObject(interfaceFromMethod.getName(), method);

            Method m = this.geneMethod(method, api);
            m.setInterfaceName(interfaceFromMethod.getName());
            super.fallbackHandler(clazz, api, m, method);
            this.addMethod(m);
        }
    }

    @Override
    public TypeFilter getTypeFilter() {
        return new AnnotationTypeFilter(GatewayService.class);
    }
}
