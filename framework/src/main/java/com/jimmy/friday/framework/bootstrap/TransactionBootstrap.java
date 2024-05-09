package com.jimmy.friday.framework.bootstrap;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.enums.transaction.TransactionPropagationEnum;
import com.jimmy.friday.boot.enums.transaction.TransactionTypeEnum;
import com.jimmy.friday.framework.annotation.transaction.Transactional;
import com.jimmy.friday.framework.base.Bootstrap;
import com.jimmy.friday.framework.base.TransactionConnectionProxy;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.transaction.TransactionContext;
import com.jimmy.friday.framework.transaction.TransactionSession;
import com.jimmy.friday.framework.transaction.def.TransactionInfo;
import com.jimmy.friday.framework.utils.ClassUtil;
import org.springframework.beans.BeansException;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;

public class TransactionBootstrap implements Bootstrap {

    private final ConfigLoad configLoad;

    private final TransactionSession transactionSession;

    public TransactionBootstrap(ConfigLoad configLoad, TransactionSession transactionSession) {
        this.configLoad = configLoad;
        this.transactionSession = transactionSession;
    }

    @Override
    public void bootstrapBefore() throws Exception {
        transactionSession.initialize();
    }

    @Override
    public void bootstrapAfter() throws Exception {
        String offsetFilePath = configLoad.getOffsetFilePath();
        if (!FileUtil.exist(offsetFilePath)) {
            FileUtil.mkdir(offsetFilePath);
        }

        transactionSession.transactionCompensation();
    }

    @Override
    public Object beanProcess(Object bean, String beanName) throws BeansException {
        //数据源管理
        Class<?> clazz = ClassUtil.getClass(bean.getClass());
        if (bean instanceof DataSource) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
                if (method.getName().equals("getConnection") && method.getReturnType().equals(Connection.class)) {
                    Connection connection = (Connection) method.invoke(bean, objects);

                    if (connection != null) {
                        TransactionContext transactionContext = TransactionContext.get();
                        if (transactionContext != null) {
                            String id = transactionContext.getId();
                            TransactionTypeEnum transactionTypeEnum = transactionContext.getTransactionTypeEnum();

                            if (StrUtil.isNotEmpty(id) && transactionTypeEnum != null) {
                                return transactionSession.getConnection(transactionTypeEnum, connection, id, beanName);
                            }
                        }

                        return connection;
                    }
                }

                return method.invoke(bean, objects);
            });

            return enhancer.create();
        }
        //事务注解
        Method[] methods = clazz.getDeclaredMethods();
        if (ArrayUtil.isNotEmpty(methods)) {
            for (Method method : methods) {
                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers)) {
                    continue;
                }
                //todo 无法循环调用
                Transactional annotation = AnnotationUtils.getAnnotation(method, Transactional.class);
                if (annotation != null) {
                    Enhancer enhancer = new Enhancer();
                    enhancer.setSuperclass(clazz);
                    enhancer.setCallback((MethodInterceptor) (o, m, objects, methodProxy) -> {
                        Transactional transactional = AnnotationUtils.getAnnotation(m, Transactional.class);
                        return transactional != null ? this.invoke(transactional, m, bean, objects, clazz, methodProxy) : m.invoke(bean, objects);
                    });

                    return enhancer.create();
                }
            }
        }

        return bean;
    }

    /**
     * 调用方法
     *
     * @param transactional
     * @param m
     * @param bean
     * @param objects
     * @return
     */
    private Object invoke(Transactional transactional, Method m, Object bean, Object[] objects, Class<?> superClass, MethodProxy methodProxy) throws Throwable {
        TransactionTypeEnum type = transactional.type();
        TransactionPropagationEnum propagation = transactional.propagation();

        switch (propagation) {
            case SUPPORTS:
                if (TransactionContext.get() == null) {
                    return methodProxy.invokeSuper(bean, objects);
                }
        }

        TransactionConnectionProxy proxy = transactionSession.getProxy(type);

        TransactionInfo info = proxy.buildTransactionInfo(transactional, m, superClass, objects);
        try {
            proxy.preExecute(info);
            Object invoke = m.invoke(bean, objects);
            proxy.afterExecute(info);
            return invoke;
        } catch (Throwable e) {
            proxy.errorExecute(info, e);
            throw e;
        } finally {
            TransactionContext transactionContext = TransactionContext.get();
            if (transactionContext != null) {
                transactionContext.release();
            }

            proxy.exitExecute(info);
        }
    }
}
