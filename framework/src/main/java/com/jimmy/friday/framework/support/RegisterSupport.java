package com.jimmy.friday.framework.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.message.gateway.ServiceReload;
import com.jimmy.friday.boot.other.ConfigConstants;
import com.jimmy.friday.framework.annotation.GatewayReference;
import com.jimmy.friday.framework.base.Register;
import com.jimmy.friday.framework.other.ConditionContextImpl;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.process.gateway.RpcProtocolInvokeProcess;
import com.jimmy.friday.framework.register.BaseRegister;
import com.jimmy.friday.framework.utils.JsonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Condition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class RegisterSupport {

    private final List<Service> services = new ArrayList<>();

    private final Map<ServiceTypeEnum, BaseRegister> registers = new HashMap<>();

    private final Map<String, Object> implementationObjectCache = new HashMap<>();

    private ConfigLoad configLoad;

    private TransmitSupport transmitSupport;

    private DefaultListableBeanFactory beanFactory;

    private MetadataReaderFactory metadataReaderFactory;

    private RpcProtocolInvokeProcess rpcProtocolInvokeProcess;

    public RegisterSupport(TransmitSupport transmitSupport, ConfigLoad configLoad, DefaultListableBeanFactory beanFactory, MetadataReaderFactory metadataReaderFactory, RpcProtocolInvokeProcess rpcProtocolInvokeProcess) {
        this.configLoad = configLoad;
        this.beanFactory = beanFactory;
        this.transmitSupport = transmitSupport;
        this.metadataReaderFactory = metadataReaderFactory;
        this.rpcProtocolInvokeProcess = rpcProtocolInvokeProcess;
    }

    public <T> T getProxy(Class<T> clazz, GatewayReference annotation) {
        ServiceTypeEnum type = annotation.type();

        String className = clazz.getName();
        if (!clazz.isInterface()) {
            throw new GatewayException(className + "不是接口类型");
        }

        String key = type + ":" + className;

        Object implementationObject = implementationObjectCache.get(key);
        if (implementationObject != null) {
            return (T) implementationObject;
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[]{clazz});
        enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
            Method m = new Method();
            m.setName(method.getName());
            m.setInterfaceName(clazz.getName());
            m.paramFill(method);

            GatewayRequest gatewayRequest = new GatewayRequest();
            gatewayRequest.setId(IdUtil.getSnowflake(1, 1).nextId());
            gatewayRequest.setRetry(annotation.retries());
            gatewayRequest.setTimeout(annotation.timeout());
            gatewayRequest.setVersion(annotation.version());
            gatewayRequest.setServiceType(type.toString());
            gatewayRequest.setMethodName(method.getName());
            gatewayRequest.setAppId(configLoad.get(ConfigConstants.APP_ID));
            gatewayRequest.setServiceName(annotation.serviceName());
            gatewayRequest.setInvokeInterface(clazz.getName());
            gatewayRequest.setClientName(configLoad.getApplicationName());
            gatewayRequest.setClientIpAddress(InetAddress.getLocalHost().getHostAddress());
            gatewayRequest.setMethodCode(SecureUtil.md5(m.key()));

            Parameter[] parameters = method.getParameters();

            if (ArrayUtil.isNotEmpty(parameters)) {
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    String name = parameter.getName();
                    String typeName = parameter.getType().getName();
                    Object object = objects[i];

                    gatewayRequest.addInvokeParam(name, typeName, object == null ? null : JsonUtil.toString(object));
                }
            }

            return responseHandler(rpcProtocolInvokeProcess.invoke(gatewayRequest), method.getGenericReturnType(), clazz);
        });

        T proxy = (T) enhancer.create();
        implementationObjectCache.put(key, proxy);
        return proxy;
    }

    public void initialize() {
        try {
            List<Class<BaseRegister>> registerClass = this.scan("com.jimmy.friday.framework.register", BaseRegister.class);
            if (CollUtil.isNotEmpty(registerClass)) {
                for (Class<BaseRegister> clazz : registerClass) {
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                    // 注册bean
                    beanFactory.registerBeanDefinition(clazz.getName(), beanDefinitionBuilder.getRawBeanDefinition());
                    BaseRegister bean = (BaseRegister) this.beanFactory.getBean(clazz.getName());
                    this.registers.put(bean.type(), bean);
                }
            }
        } catch (Exception e) {
            throw new GatewayException(e);
        }
        //服务重载推送
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            List<Service> services = this.getServices();
            if (CollUtil.isNotEmpty(services)) {
                ServiceReload serviceReload = new ServiceReload();
                serviceReload.setId(configLoad.getId());
                serviceReload.setServices(services);
                transmitSupport.broadcast(serviceReload);
            }
        }, 1, 3, TimeUnit.MINUTES);
    }

    public List<Service> getServices() {
        if (CollUtil.isEmpty(this.services)) {
            this.getServicesWithMethod();
        }

        return this.services;
    }

    public List<Service> getServicesWithMethod() {
        try {
            if (MapUtil.isNotEmpty(registers)) {
                Collection<BaseRegister> values = registers.values();
                for (Register register : values) {
                    List<Method> methods = register.getMethods();
                    if (CollUtil.isEmpty(methods)) {
                        continue;
                    }

                    register.register();
                    Service service = register.getService();
                    service.setApplicationId(configLoad.getId());
                    service.setVersion(configLoad.getVersion());
                    service.setWeight(configLoad.getWeight());
                    service.setGroup(configLoad.get(ConfigConstants.GROUP_NAME));
                    service.setMethods(methods);
                    service.setServiceId(this.getServiceId(service));
                    services.add(service);
                }
            }

            return services;
        } catch (Exception e) {
            throw new GatewayException(e);
        }
    }

    public void collectMethod(String className) {
        try {
            if (MapUtil.isNotEmpty(registers)) {
                Collection<BaseRegister> values = registers.values();
                Class<?> clazz = Class.forName(className);

                if (this.classFilter(clazz)) {
                    return;
                }

                for (Register register : values) {
                    register.collectMethod(clazz);
                }
            }
        } catch (Exception e) {
            throw new GatewayException(e);
        }
    }

    public List<TypeFilter> getTypeFilters() {
        List<TypeFilter> typeFilters = new ArrayList<>();

        if (MapUtil.isNotEmpty(registers)) {
            Collection<BaseRegister> values = registers.values();
            for (Register register : values) {
                TypeFilter typeFilter = register.getTypeFilter();
                if (typeFilter != null) {
                    typeFilters.add(typeFilter);
                }
            }
        }

        return typeFilters;
    }

    /**
     * class过滤
     *
     * @param clazz
     * @return
     */
    private boolean classFilter(Class<?> clazz) {
        if (clazz.isEnum()) {
            return true;
        }

        if (clazz.isAnnotation()) {
            return true;
        }

        return false;
    }

    /**
     * 类扫描
     *
     * @param scanPath
     * @param baseClass
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> List<Class<T>> scan(String scanPath, Class<T> baseClass) throws Exception {
        List<Class<T>> classes = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        // 扫描带有自定义注解的类
        provider.addIncludeFilter(new AnnotationTypeFilter(com.jimmy.friday.framework.annotation.Condition.class));
        //初始化condition上下文
        ConditionContextImpl conditionContext = new ConditionContextImpl(beanFactory);
        Set<BeanDefinition> scanList = provider.findCandidateComponents(scanPath);
        for (BeanDefinition bean : scanList) {
            //判断是否跳过
            if (shouldSkip(bean, conditionContext)) {
                continue;
            }

            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(bean.getBeanClassName());
            com.jimmy.friday.framework.annotation.Condition annotation = AnnotationUtils.getAnnotation(clazz, com.jimmy.friday.framework.annotation.Condition.class);
            if (annotation != null) {
                if (baseClass.isInterface()) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces != null && interfaces.length > 0) {
                        for (Class<?> anInterface : interfaces) {
                            if (anInterface.equals(baseClass)) {
                                classes.add((Class<T>) clazz);
                            }
                        }
                    }

                } else {
                    Class<?> superclass = clazz.getSuperclass();
                    if (superclass.equals(baseClass)) {
                        classes.add((Class<T>) clazz);
                    }
                }
            }
        }

        return classes;
    }

    /**
     * 判断是否需要跳过
     *
     * @return
     */
    private boolean shouldSkip(BeanDefinition bean, ConditionContextImpl conditionContext) throws IOException {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(bean.getBeanClassName());

        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        if (metadata == null || !metadata.isAnnotated(com.jimmy.friday.framework.annotation.Condition.class.getName())) {
            return true;
        }

        List<Condition> conditions = new ArrayList<>();

        MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(com.jimmy.friday.framework.annotation.Condition.class.getName(), true);
        Object values = (attributes != null ? attributes.get("condition") : null);
        List<String> conditionArray = (List<String>) (values != null ? values : Collections.emptyList());

        for (String conditionClass : conditionArray) {
            Class<?> conditionClazz = ClassUtils.resolveClassName(conditionClass, Thread.currentThread().getContextClassLoader());
            Condition condition = (Condition) BeanUtils.instantiateClass(conditionClazz);
            conditions.add(condition);
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

    /**
     * 响应处理
     *
     * @param gatewayResponse
     * @return
     */
    private Object responseHandler(GatewayResponse gatewayResponse, Type type, Class<?> sourceClass) {
        String error = gatewayResponse.getError();
        Boolean isSuccess = gatewayResponse.getIsSuccess();
        String jsonResult = gatewayResponse.getJsonResult();
        String exceptionClass = gatewayResponse.getExceptionClass();

        if (isSuccess) {
            return StrUtil.isNotEmpty(jsonResult) && type != null ? JsonUtil.deserialize(jsonResult, type, sourceClass) : null;
        }

        try {
            Class<?> exceptionClazz = this.getExceptionClass(exceptionClass);
            if (exceptionClazz == null || exceptionClazz.equals(GatewayException.class)) {
                throw new GatewayException(error);
            }

            Constructor<?> constructor = exceptionClazz.getConstructor(String.class);
            Object o = constructor.newInstance(error);

            if (o instanceof Exception) {
                throw (Exception) o;
            } else {
                throw new GatewayException(error);
            }
        } catch (Exception e) {
            throw new GatewayException(error);
        }
    }

    /**
     * 获取异常类
     *
     * @param exceptionClass
     * @return
     */
    private Class<?> getExceptionClass(String exceptionClass) {
        try {
            return Class.forName(exceptionClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取服务id
     *
     * @param service
     * @return
     */
    private String getServiceId(Service service) {
        return SecureUtil.md5(service.getName() + service.getIpAddress() + service.getPort() + service.getVersion());
    }

}
