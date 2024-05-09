package com.jimmy.friday.framework.bootstrap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.framework.annotation.gateway.GatewayReference;
import com.jimmy.friday.framework.base.Bootstrap;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.other.gateway.GatewayClassPathBeanDefinitionScanner;
import com.jimmy.friday.framework.support.InvokeSupport;
import com.jimmy.friday.framework.support.RegisterSupport;
import com.jimmy.friday.framework.utils.ClassUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.TypeFilter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class GatewayBootstrap implements Bootstrap {

    private ConfigLoad configLoad;

    private InvokeSupport invokeSupport;

    private RegisterSupport registerSupport;

    public GatewayBootstrap(RegisterSupport registerSupport, ConfigLoad configLoad, InvokeSupport invokeSupport) {
        this.configLoad = configLoad;
        this.invokeSupport = invokeSupport;
        this.registerSupport = registerSupport;
    }

    @Override
    public Object beanProcess(Object bean, String beanName) throws BeansException {
        //调用实现类
        invokeSupport.invokeInterfaceImplementationObject(bean);
        //GatewayReference 注解处理
        this.gatewayReferenceHandler(bean);
        return bean;
    }

    @Override
    public void bootstrapAfter() throws Exception {

    }

    @Override
    public void bootstrapBefore() throws Exception {
        registerSupport.initialize();

        GatewayClassPathBeanDefinitionScanner provider = new GatewayClassPathBeanDefinitionScanner(false);

        provider.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            // 获取当前扫描到的类的类元数据
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            // 排除接口、枚举和抽象类
            return !classMetadata.isInterface() && !classMetadata.isAbstract() && !classMetadata.isAnnotation();
        });

        List<TypeFilter> typeFilters = registerSupport.getTypeFilters();
        if (CollUtil.isNotEmpty(typeFilters)) {
            for (TypeFilter typeFilter : typeFilters) {
                provider.addIncludeFilter(typeFilter);
            }
        }

        for (String packagePath : configLoad.getGatewayPackagesToScan()) {
            //自定义函数参数处理
            Set<BeanDefinition> scanList = provider.findCandidateComponents(packagePath);
            if (CollUtil.isEmpty(scanList)) {
                continue;
            }

            for (BeanDefinition beanDefinition : scanList) {
                registerSupport.collectMethod(beanDefinition.getBeanClassName());
            }
        }
    }


    /**
     * GatewayReference注解处理
     *
     * @param bean
     */
    private void gatewayReferenceHandler(Object bean) {
        try {
            Class<?> clazz = ClassUtil.getClass(bean.getClass());

            Field[] declaredFields = clazz.getDeclaredFields();
            if (ArrayUtil.isEmpty(declaredFields)) {
                return;
            }

            for (Field field : declaredFields) {
                GatewayReference annotation = AnnotationUtils.findAnnotation(field, GatewayReference.class);
                if (annotation == null) {
                    continue;
                }

                if (!field.getType().isInterface()) {
                    continue;
                }

                Object proxy = registerSupport.getProxy(field.getType(), annotation);
                if (proxy == null) {
                    throw new GatewayException("provider代理失败");
                }

                field.setAccessible(true);
                field.set(bean, proxy);
            }
        } catch (Exception e) {
            throw new GatewayException(e);
        }
    }
}
