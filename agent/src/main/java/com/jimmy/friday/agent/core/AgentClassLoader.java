package com.jimmy.friday.agent.core;

import com.jimmy.friday.boot.other.ConfigConstants;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class AgentClassLoader extends ClassLoader {

    private ClassLoader parent;

    private static AgentClassLoader DEFAULT_LOADER;

    static {
        Method[] methods = ClassLoader.class.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();
            if ("registerAsParallelCapable".equalsIgnoreCase(methodName)) {
                try {
                    method.setAccessible(true);
                    method.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    public AgentClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;
    }

    public static AgentClassLoader initDefaultLoader() {
        if (DEFAULT_LOADER == null) {
            synchronized (AgentClassLoader.class) {
                if (DEFAULT_LOADER == null) {
                    DEFAULT_LOADER = new AgentClassLoader(PluginBootstrap.class.getClassLoader());
                }
            }
        }
        return getDefault();
    }

    public static AgentClassLoader getDefault() {
        return DEFAULT_LOADER;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(ConfigConstants.AGENT_ACTION_PATH)) {
            try {
                Class<?> c = findLoadedClass(name);
                if (c != null) {
                    return c;
                }

                String fileName = name.replace('.', '/').concat(".class");
                InputStream is = parent.getResourceAsStream(fileName);
                if (is == null) {
                    return super.loadClass(name);
                }
                byte[] b = new byte[is.available()];
                is.read(b);
                return defineClass(name, b, 0, b.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(name);
            }
        }

        return super.loadClass(name);
    }
}
