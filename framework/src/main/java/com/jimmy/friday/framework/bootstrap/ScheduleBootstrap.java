package com.jimmy.friday.framework.bootstrap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;
import com.jimmy.friday.boot.enums.JobRunStatusEnum;
import com.jimmy.friday.boot.exception.ScheduleException;
import com.jimmy.friday.boot.message.schedule.ScheduleHeartbeat;
import com.jimmy.friday.framework.annotation.Job;
import com.jimmy.friday.framework.base.Bootstrap;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.other.GatewayClassPathBeanDefinitionScanner;
import com.jimmy.friday.framework.schedule.ScheduleCenter;
import com.jimmy.friday.framework.schedule.ScheduleExecutor;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.utils.ClassUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.ClassMetadata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduleBootstrap implements Bootstrap {

    private ConfigLoad configLoad;

    private ScheduleCenter scheduleCenter;

    private TransmitSupport transmitSupport;

    private ScheduleExecutor scheduleExecutor;

    public ScheduleBootstrap(TransmitSupport transmitSupport, ConfigLoad configLoad, ScheduleCenter scheduleCenter, ScheduleExecutor scheduleExecutor) {
        this.configLoad = configLoad;
        this.scheduleCenter = scheduleCenter;
        this.transmitSupport = transmitSupport;
        this.scheduleExecutor = scheduleExecutor;
    }

    @Override
    public void bootstrapBefore() throws Exception {
        GatewayClassPathBeanDefinitionScanner provider = new GatewayClassPathBeanDefinitionScanner(false);

        provider.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            // 获取当前扫描到的类的类元数据
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            // 排除接口、枚举和抽象类
            return !classMetadata.isInterface() && !classMetadata.isAbstract() && !classMetadata.isAnnotation();
        });

        for (String packagePath : configLoad.getGatewayPackagesToScan()) {
            //自定义函数参数处理
            Set<BeanDefinition> scanList = provider.findCandidateComponents(packagePath);
            if (CollUtil.isEmpty(scanList)) {
                continue;
            }

            for (BeanDefinition beanDefinition : scanList) {
                this.loadBeanDefinition(beanDefinition);
            }
        }
    }

    @Override
    public void bootstrapAfter() throws Exception {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            ScheduleHeartbeat scheduleHeartbeat = new ScheduleHeartbeat();
            scheduleHeartbeat.setApplicationId(configLoad.getId());
            scheduleHeartbeat.setApplicationName(configLoad.getApplicationName());
            scheduleHeartbeat.setScheduleRunInfoList(scheduleExecutor.getRunInfo());
            transmitSupport.send(scheduleHeartbeat);
        }, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public Object beanProcess(Object bean, String beanName) throws BeansException {
        //数据源管理
        Class<?> clazz = ClassUtil.getClass(bean.getClass());

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            Job annotation = AnnotationUtils.getAnnotation(method, Job.class);
            if (annotation != null) {
                scheduleCenter.setSpringBeanId(annotation.id(), beanName);
            }
        }

        return bean;
    }

    /**
     * bean加载
     *
     * @param beanDefinition
     */
    private void loadBeanDefinition(BeanDefinition beanDefinition) throws Exception {
        String beanClassName = beanDefinition.getBeanClassName();

        Class<?> clazz = Class.forName(beanClassName);

        Method[] methods = clazz.getMethods();
        if (ArrayUtil.isEmpty(methods)) {
            return;
        }

        for (Method method : methods) {
            Job annotation = AnnotationUtils.getAnnotation(method, Job.class);
            if (annotation != null) {
                Class<?> returnType = method.getReturnType();
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new ScheduleException("方法:" + method.getName() + "参数长度不符合");
                }

                Class<?> parameterType = parameterTypes[0];
                if (!parameterType.equals(ScheduleContext.class)) {
                    throw new ScheduleException("方法:" + method.getName() + "参数类型不符合，入参需要ScheduleContext");
                }

                if (!returnType.equals(ScheduleResult.class)) {
                    throw new ScheduleException("方法:" + method.getName() + "返回类型不符合，需要返回ScheduleResult");
                }

                scheduleCenter.register(beanClassName, method.getName(), annotation.id());
            }
        }
    }
}
