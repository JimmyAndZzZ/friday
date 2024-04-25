package com.jimmy.friday.framework.other;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.framework.annotation.EnableGateway;
import com.jimmy.friday.framework.core.ConfigLoad;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class GatewayScanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        //获取注解路径属性
        Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);

        ConfigLoad configLoad = beanFactory.getBean(ConfigLoad.class);
        configLoad.setGatewayPackagesToScan(packagesToScan);
    }

    /**
     * 获取包路径
     *
     * @param metadata
     * @return
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(EnableGateway.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(basePackages));
        if (CollUtil.isEmpty(packagesToScan)) {
            packagesToScan.add(ClassUtils.getPackageName(metadata.getClassName()));
        }
        packagesToScan.removeIf((candidate) -> !StringUtils.hasText(candidate));
        return packagesToScan;
    }
}
