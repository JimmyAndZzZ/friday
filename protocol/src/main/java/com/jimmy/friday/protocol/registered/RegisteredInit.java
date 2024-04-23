package com.jimmy.friday.protocol.registered;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.protocol.annotations.Protocol;
import com.jimmy.friday.protocol.annotations.Send;
import com.jimmy.friday.protocol.annotations.Listen;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.base.Push;
import com.jimmy.friday.protocol.base.Registered;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.config.ProtocolProperties;
import com.jimmy.friday.protocol.core.FallbackProxy;
import com.jimmy.friday.protocol.core.InputProxy;
import com.jimmy.friday.protocol.core.ProtocolFactory;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Slf4j
public class RegisteredInit implements BeanPostProcessor, Ordered {

    private static final String FALLBACK_GROUP_NAME = "FALLBACK_GROUP_NAME";

    private Map<String, InputProxy> proxyMap = Maps.newHashMap();

    @Autowired
    private ProtocolProperties protocolProperties;

    @Autowired
    private ProtocolFactory protocolFactory;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            //判断是否为代理
            boolean isProxy = AopUtils.isAopProxy(bean);
            Object target = AopProxyUtils.getSingletonTarget(bean);
            if (target == null || !isProxy) {
                target = bean;
            }

            Class<?> targetClass = AopUtils.getTargetClass(bean);
            //获取字段列表
            List<Field> declaredFields = Lists.newArrayList();
            this.getFields(targetClass, declaredFields);

            Map<ProtocolEnum, ProtocolProperty> protocols = protocolProperties.getProtocols();
            Method[] methods = target.getClass().getMethods();
            if (ArrayUtil.isNotEmpty(methods)) {
                for (Method method : methods) {
                    Listen annotation = AnnotationUtils.getAnnotation(method, Listen.class);
                    if (annotation == null) {
                        continue;
                    }

                    this.listen(annotation, bean, method);
                }
            }

            if (CollUtil.isNotEmpty(declaredFields)) {
                for (Field field : declaredFields) {
                    if (!field.getType().equals(Output.class) && !field.getType().equals(Push.class)) {
                        continue;
                    }

                    Send annotation = AnnotationUtils.getAnnotation(field, Send.class);
                    if (annotation == null) {
                        continue;
                    }

                    Protocol send = annotation.send();
                    Protocol[] fallback = annotation.fallback();

                    ProtocolProperty protocolProperty = protocols.get(send.protocol());
                    if (protocolProperty == null) {
                        throw new IllegalArgumentException("未找到相关协议注册类");
                    }

                    Registered registeredByType = protocolFactory.getRegisteredByTypeAndCheck(send.protocol(), protocolProperty);
                    //构建通信协议
                    com.jimmy.friday.protocol.core.Protocol protocol = com.jimmy.friday.protocol.core.Protocol.buildOutput(send);
                    if (field.getType().equals(Push.class)) {
                        protocol.setIsWsServerToClient(true);
                    }

                    Output output = registeredByType.registeredClient(protocol);
                    field.setAccessible(true);
                    field.set(isProxy ? target : bean, output);

                    if (ArrayUtil.isNotEmpty(fallback)) {
                        if (!isHystrix()) {
                            log.error("没有提供熔断jar包");
                        } else {
                            Protocol an = fallback[0];
                            //构建通信协议
                            com.jimmy.friday.protocol.core.Protocol fallbakProtocol = com.jimmy.friday.protocol.core.Protocol.buildOutput(an);
                            if (field.getType().equals(Push.class)) {
                                fallbakProtocol.setIsWsServerToClient(true);
                            }

                            ProtocolProperty fallbackPro = protocols.get(an.protocol());
                            if (fallbackPro == null) {
                                throw new IllegalArgumentException("未找到相关协议注册类");
                            }

                            //注入熔断代理类
                            Output fallbackOutput = protocolFactory.getRegisteredByTypeAndCheck(an.protocol(), fallbackPro).registeredClient(fallbakProtocol);
                            Output fallbackProxy = message -> {
                                FallbackProxy f = new FallbackProxy(FALLBACK_GROUP_NAME, output, fallbackOutput, message);
                                return f.execute();
                            };

                            field.set(isProxy ? target : bean, fallbackProxy);
                        }
                    }
                }
            }

            return bean;
        } catch (Exception e) {
            log.error("协议初始化失败", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    /**
     * 监听
     *
     * @param listen
     * @param o
     * @param m
     */
    private void listen(Listen listen, Object o, Method m) throws Exception {
        Map<ProtocolEnum, ProtocolProperty> protocols = protocolProperties.getProtocols();
        ProtocolProperty listenPro = protocols.get(listen.protocol().protocol());
        if (listenPro == null) {
            throw new IllegalArgumentException("未找到相关协议注册类");
        }

        com.jimmy.friday.protocol.core.Protocol protocolInfo = com.jimmy.friday.protocol.core.Protocol.buildInput(listen);
        ProtocolEnum protocol = listen.protocol().protocol();

        Registered byType = protocolFactory.getRegisteredByTypeAndCheck(protocol, listenPro);
        byType.registeredServer(protocolInfo, this.inputInit(protocolInfo, protocol, o, m));
    }

    /**
     * 监听类初始化
     *
     * @param protocolInfo
     * @param protocol
     * @param o
     * @param m
     * @return
     */
    private InputProxy inputInit(com.jimmy.friday.protocol.core.Protocol protocolInfo, ProtocolEnum protocol, Object o, Method m) {
        String topic = protocolInfo.getTopic();
        String groupId = protocolInfo.getGroupId();

        String key = protocol.getMessage() + ":" + topic + ":" + groupId;
        if (proxyMap.containsKey(key)) {
            return proxyMap.get(key);
        }

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(InputProxy.class);
        beanDefinitionBuilder.addPropertyValue("o", o);
        beanDefinitionBuilder.addPropertyValue("m", m);
        // 注册bean
        beanFactory.registerBeanDefinition(key + "Input", beanDefinitionBuilder.getRawBeanDefinition());
        InputProxy bean = (InputProxy) this.beanFactory.getBean(key + "Input");
        proxyMap.put(key, bean);
        return bean;
    }

    /**
     * 判断是否包含熔断
     *
     * @return
     */
    private boolean isHystrix() {
        try {
            boolean b = null != Class.forName("com.netflix.hystrix.HystrixCommand");
            return b;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取类所有字段
     *
     * @param clazz
     * @param fields
     */
    private void getFields(Class<?> clazz, List<Field> fields) {
        if (clazz == null || clazz.equals(Object.class)) {
            return;
        }

        getFields(clazz.getSuperclass(), fields);

        Field[] declaredFields = clazz.getDeclaredFields();
        if (ArrayUtil.isNotEmpty(declaredFields)) {
            fields.addAll(CollUtil.toList(declaredFields));
        }
    }
}
