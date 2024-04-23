package com.jimmy.friday.protocol.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.protocol.annotations.RegisteredType;
import com.jimmy.friday.protocol.exception.ProtocolException;
import com.jimmy.friday.protocol.registered.BaseRegistered;
import com.jimmy.friday.protocol.condition.ConditionContextImpl;
import com.jimmy.friday.protocol.config.ProtocolProperties;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Condition;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.*;

public class ProtocolFactory implements ApplicationListener<ContextClosedEvent> {

    private static final String REGISTERED_PATH = "com.jimmy.friday.protocol.registered";

    private Map<String, BaseRegistered> registeredHashMap = Maps.newHashMap();

    private Map<ProtocolEnum, Class<? extends BaseRegistered>> registereds = Maps.newHashMap();

    @Autowired
    private Environment environment;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private MetadataReaderFactory metadataReaderFactory;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (MapUtil.isNotEmpty(registeredHashMap)) {
            for (Map.Entry<String, BaseRegistered> entry : registeredHashMap.entrySet()) {
                entry.getValue().close();
            }
        }
    }

    public BaseRegistered getRegisteredByType(ProtocolEnum type, ProtocolProperty protocolProperty) {
        try {
            String key = new StringBuilder(type.getMessage()).append(":").append(protocolProperty.toString()).toString();

            BaseRegistered baseRegistered = registeredHashMap.get(key);
            if (baseRegistered == null) {
                Class<? extends BaseRegistered> clazz = registereds.get(type);
                if (clazz == null) {
                    throw new IllegalArgumentException("未找到对应数据类型");
                }

                baseRegistered = clazz.newInstance();
                baseRegistered.setEnvironment(environment);
                baseRegistered.setBeanFactory(beanFactory);
                baseRegistered.init(protocolProperty);
                registeredHashMap.put(key, baseRegistered);
            }

            return baseRegistered;
        } catch (Exception e) {
            throw new ProtocolException(e.getMessage());
        }
    }

    public BaseRegistered getRegisteredByTypeAndCheck(ProtocolEnum type, ProtocolProperty protocolProperty) {
        BaseRegistered registeredByType = this.getRegisteredByType(type, protocolProperty);
        if (registeredByType == null) {
            throw new IllegalArgumentException("未找到有效注册器");
        }
        return registeredByType;
    }

    public BaseRegistered getRegisteredByTypeAndCheck(ProtocolEnum type) {
        ProtocolProperties bean = beanFactory.getBean(ProtocolProperties.class);
        Map<ProtocolEnum, ProtocolProperty> protocols = bean.getProtocols();

        ProtocolProperty protocolProperty = protocols.get(type);
        if (protocolProperty == null) {
            throw new IllegalArgumentException("未找到相关协议配置");
        }

        BaseRegistered registeredByType = this.getRegisteredByType(type, protocolProperty);
        if (registeredByType == null) {
            throw new IllegalArgumentException("未找到有效注册器");
        }
        return registeredByType;
    }


    /**
     * 注册器初始化
     */
    public void registeredInit() throws Exception {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        // 扫描带有自定义注解的类
        provider.addIncludeFilter(new AnnotationTypeFilter(RegisteredType.class));

        List<String> paths = Lists.newArrayList(REGISTERED_PATH);
        //初始化condition上下文
        ConditionContextImpl conditionContext = new ConditionContextImpl(beanFactory);
        for (String path : paths) {
            Set<BeanDefinition> scanList = provider.findCandidateComponents(path);
            for (BeanDefinition bean : scanList) {
                //判断是否跳过
                if (shouldSkip(bean, conditionContext)) {
                    continue;
                }

                Class<?> clazz = Class.forName(bean.getBeanClassName());
                RegisteredType annotation = AnnotationUtils.getAnnotation(clazz, RegisteredType.class);

                if (annotation != null) {
                    Class<?> superclass = clazz.getSuperclass();
                    if (superclass.equals(BaseRegistered.class)) {
                        registereds.put(annotation.type(), (Class<? extends BaseRegistered>) clazz);
                    }
                }
            }
        }
    }

    /**
     * 判断是否需要跳过
     *
     * @return
     */
    private boolean shouldSkip(BeanDefinition bean, ConditionContextImpl conditionContext) throws IOException {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(bean.getBeanClassName());

        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        if (metadata == null || !metadata.isAnnotated(RegisteredType.class.getName())) {
            return true;
        }

        List<Condition> conditions = new ArrayList<>();

        MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(RegisteredType.class.getName(), true);
        Object values = (attributes != null ? attributes.get("condition") : null);
        List<String[]> conditionArray = (List<String[]>) (values != null ? values : Collections.emptyList());

        for (String[] conditionClasses : conditionArray) {
            for (String conditionClass : conditionClasses) {
                Class<?> conditionClazz = ClassUtils.resolveClassName(conditionClass, ClassLoaderUtil.getClassLoader());
                Condition condition = (Condition) BeanUtils.instantiateClass(conditionClazz);
                conditions.add(condition);
            }
        }

        if (CollUtil.isEmpty(conditions)) {
            return false;
        }

        for (Condition condition : conditions) {
            if (!condition.matches(conditionContext, metadata)) {
                return true;
            }
        }

        return false;
    }
}
