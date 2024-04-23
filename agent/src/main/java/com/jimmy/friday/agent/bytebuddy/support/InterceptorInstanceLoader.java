package com.jimmy.friday.agent.bytebuddy.support;

import com.jimmy.friday.agent.core.AgentClassLoader;

import java.util.concurrent.ConcurrentHashMap;

public class InterceptorInstanceLoader {

    private static ConcurrentHashMap<String, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();

    public static <T> T load(String className, ClassLoader targetClassLoader) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        String instanceKey = className + "_OF_" + targetClassLoader.getClass()
                .getName() + "@" + Integer.toHexString(targetClassLoader
                .hashCode());
        Object inst = INSTANCE_CACHE.get(instanceKey);
        if (inst == null) {
            inst = Class.forName(className, true, new AgentClassLoader(targetClassLoader)).newInstance();
            if (inst != null) {
                INSTANCE_CACHE.put(instanceKey, inst);
            }
        }

        return (T) inst;
    }
}
