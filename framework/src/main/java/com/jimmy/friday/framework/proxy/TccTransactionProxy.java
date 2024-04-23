package com.jimmy.friday.framework.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.TransactionStatusEnum;
import com.jimmy.friday.boot.enums.TransactionTypeEnum;
import com.jimmy.friday.framework.annotation.Transactional;
import com.jimmy.friday.framework.core.GlobalCache;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.transaction.def.TransactionInfo;
import com.jimmy.friday.framework.transaction.TransactionSession;
import com.jimmy.friday.framework.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TccTransactionProxy extends BaseTransactionProxy implements ApplicationContextAware {

    private final Map<Class<?>, Object> instance = new HashMap<>();

    private ApplicationContext applicationContext;

    public TccTransactionProxy(GlobalCache globalCache, TransactionSession transactionSession, TransmitSupport transmitSupport) {
        super(globalCache, transactionSession, transmitSupport);
    }

    @Override
    public Connection getConnection(Connection connection, String id, String dsName) {
        return connection;
    }

    @Override
    public void callback(TransactionFacts transactionFacts) {
        String transactionId = transactionFacts.getTransactionId();
        String executeClass = transactionFacts.getExecuteClass();
        String confirmMethod = transactionFacts.getConfirmMethod();
        String cancelMethod = transactionFacts.getCancelMethod();
        List<InvokeParam> invokeParams = transactionFacts.getInvokeParams();
        TransactionStatusEnum transactionStatus = transactionFacts.getTransactionStatus();

        Class<?> clazz = ClassUtil.loadClass(executeClass);
        switch (transactionStatus) {
            case SUCCESS:
                if (StrUtil.isNotEmpty(confirmMethod)) {
                    log.info("准备tcc confirm,事务id:{}", transactionId);

                    Object instanceObject = this.getInstanceObject(clazz);

                    if (instanceObject == null) {
                        log.error("{}获取实例失败,事务id:{}", executeClass, transactionId);
                        return;
                    }

                    Method method = this.findMethod(clazz, confirmMethod, invokeParams);
                    if (method == null) {
                        log.error("{}:confirmMethod方法不存在,事务id:{}", confirmMethod, transactionFacts);
                        return;
                    }

                    ArrayList<Object> arrays = new ArrayList<>();
                    if (CollUtil.isNotEmpty(invokeParams)) {
                        Type[] genericParameterTypes = method.getGenericParameterTypes();

                        for (int i = 0; i < invokeParams.size(); i++) {
                            InvokeParam invokeParam = invokeParams.get(i);
                            Type type = genericParameterTypes[i];

                            String jsonData = invokeParam.getJsonData();

                            if (StrUtil.isEmpty(jsonData)) {
                                arrays.add(null);
                                continue;
                            }

                            arrays.add(JsonUtil.deserialize(jsonData, type, clazz));
                        }
                    }

                    try {
                        method.invoke(instanceObject, CollUtil.isEmpty(arrays) ? null : arrays.toArray());
                    } catch (Exception e) {
                        log.error("tcc 确认方法执行失败，事务id:{}", transactionFacts, e);
                    }

                    log.info("完成tcc confirm,事务id:{}", transactionId);
                }

                break;
            case TIMEOUT:
            case FAIL:
            case INTERRUPT:
                if (StrUtil.isNotEmpty(cancelMethod)) {
                    log.info("准备tcc cancel,事务id:{}", transactionId);

                    Object instanceObject = this.getInstanceObject(clazz);

                    if (instanceObject == null) {
                        log.error("{}获取实例失败,事务id:{}", executeClass, transactionId);
                        return;
                    }

                    Method method = this.findMethod(clazz, cancelMethod, invokeParams);
                    if (method == null) {
                        log.error("{}:cancelMethod方法不存在,事务id:{}", cancelMethod, transactionFacts);
                        return;
                    }

                    ArrayList<Object> arrays = new ArrayList<>();
                    if (CollUtil.isNotEmpty(invokeParams)) {
                        Type[] genericParameterTypes = method.getGenericParameterTypes();

                        for (int i = 0; i < invokeParams.size(); i++) {
                            InvokeParam invokeParam = invokeParams.get(i);
                            Type type = genericParameterTypes[i];

                            String jsonData = invokeParam.getJsonData();

                            if (StrUtil.isEmpty(jsonData)) {
                                arrays.add(null);
                                continue;
                            }

                            arrays.add(JsonUtil.deserialize(jsonData, type, clazz));
                        }
                    }

                    try {
                        method.invoke(instanceObject, CollUtil.isEmpty(arrays) ? null : arrays.toArray());
                    } catch (Exception e) {
                        log.error("tcc 回滚方法执行失败，事务id:{}", transactionFacts, e);
                    }

                    log.info("完成tcc cancel,事务id:{}", transactionId);
                }

                break;
        }
    }

    @Override
    public TransactionInfo buildTransactionInfo(Transactional transactional, Method method, Class<?> superClass, Object[] objects) {
        Class<?> clazz = transactional.executeClass();

        TransactionInfo transactionInfo = super.buildTransactionInfo(transactional, method, superClass, objects);
        transactionInfo.setExecuteClass(clazz.equals(Void.class) ? superClass : clazz);

        Parameter[] parameters = method.getParameters();
        if (ArrayUtil.isNotEmpty(parameters)) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String name = parameter.getName();
                String typeName = parameter.getType().getName();
                Object object = objects[i];

                transactionInfo.addInvokeParam(name, typeName, object == null ? null : JsonUtil.toString(object));
            }
        }

        return transactionInfo;
    }

    @Override
    public void preExecute(TransactionInfo info) {
        super.preExecute(info);

        Class<?> executeClass = info.getExecuteClass();
        String confirmMethod = info.getConfirmMethod();
        String cancelMethod = info.getCancelMethod();

        if (StrUtil.isEmpty(confirmMethod) && StrUtil.isEmpty(cancelMethod)) {
            return;
        }

        TransactionFacts transactionFacts = new TransactionFacts();
        transactionFacts.setExecuteClass(executeClass.getName());
        transactionFacts.setCancelMethod(cancelMethod);
        transactionFacts.setTransactionId(info.getId());
        transactionFacts.setConfirmMethod(confirmMethod);
        transactionFacts.setTransactionType(this.type());
        transactionFacts.setInvokeParams(info.getInvokeParams());
        transactionSession.collectTransaction(transactionFacts);
    }

    @Override
    public void afterExecute(TransactionInfo info) {
        super.afterExecute(info);
    }

    @Override
    public void errorExecute(TransactionInfo info, Throwable throwable) {
        super.errorExecute(info, throwable);
    }

    @Override
    public TransactionTypeEnum type() {
        return TransactionTypeEnum.TCC;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取执行方法
     *
     * @param clazz
     * @param methodName
     * @param invokeParams
     * @return
     */
    private Method findMethod(Class<?> clazz, String methodName, List<InvokeParam> invokeParams) {
        List<String> paramClassNames = CollUtil.isNotEmpty(invokeParams) ? invokeParams.stream().map(InvokeParam::getClassName).collect(Collectors.toList()) : new ArrayList<>();

        Method[] declaredMethods = clazz.getDeclaredMethods();
        if (ArrayUtil.isEmpty(declaredMethods)) {
            return null;
        }

        for (Method declaredMethod : declaredMethods) {
            if (!methodName.equals(declaredMethod.getName())) {
                continue;
            }

            if (this.isMethodParamEqual(declaredMethod.getParameterTypes(), paramClassNames)) {
                return declaredMethod;
            }
        }

        return null;
    }

    /**
     * 方法参数是否相同
     *
     * @param parameterTypes
     * @param paramClassNames
     * @return
     */
    private boolean isMethodParamEqual(Class<?>[] parameterTypes, List<String> paramClassNames) {
        if (parameterTypes.length != paramClassNames.size()) {
            return false;
        }

        if (parameterTypes.length == 0) {
            return true;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            String name = parameterType.getName();
            String paramClassName = paramClassNames.get(i);

            if (!com.jimmy.friday.framework.utils.ClassUtil.classEqual(name, paramClassName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取实例
     *
     * @param clazz
     * @return
     */
    private Object getInstanceObject(Class<?> clazz) {
        Object o = instance.get(clazz);
        if (o != null) {
            return o;
        }

        try {
            Object bean = applicationContext.getBean(clazz);
            instance.put(clazz, bean);
            return bean;
        } catch (BeansException ignored) {

        }

        try {
            Object bean = clazz.newInstance();
            instance.put(clazz, bean);
            return bean;
        } catch (Exception e) {
            return null;
        }
    }
}
