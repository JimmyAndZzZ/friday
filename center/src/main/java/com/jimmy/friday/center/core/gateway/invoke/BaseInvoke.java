package com.jimmy.friday.center.core.gateway.invoke;

import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.base.gateway.Invoke;

import java.lang.reflect.Constructor;

public abstract class BaseInvoke implements Invoke {

    /**
     * 异常类生成
     *
     * @param error
     * @param exceptionClass
     * @return
     */
    protected Exception geneException(String error, String exceptionClass) {
        try {
            Class<?> exceptionClazz = this.getExceptionClass(exceptionClass);
            if (exceptionClazz == null || exceptionClazz.equals(GatewayException.class)) {
                return new GatewayException(error);
            }

            Constructor<?> constructor = exceptionClazz.getConstructor(String.class);
            Object o = constructor.newInstance(error);

            if (o instanceof Exception) {
                return (Exception) o;
            } else {
                return new GatewayException(error);
            }
        } catch (Exception e) {
            return new GatewayException(error);
        }
    }

    /**
     * 获取异常类
     *
     * @param exceptionClass
     * @return
     */
    protected Class<?> getExceptionClass(String exceptionClass) {
        try {
            return Class.forName(exceptionClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
    