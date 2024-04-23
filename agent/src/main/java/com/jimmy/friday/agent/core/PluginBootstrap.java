package com.jimmy.friday.agent.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.IndirectMatch;
import com.jimmy.friday.agent.match.NameMatch;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import com.jimmy.friday.agent.plugin.define.base.CmdEnhancePluginDefine;
import com.jimmy.friday.agent.plugin.define.base.DefaultEnhancePluginDefine;
import com.jimmy.friday.agent.support.CommandSupport;
import net.bytebuddy.description.type.TypeDescription;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginBootstrap {

    private static final String PACKAGE_PATH = "com.jimmy.friday.agent.plugin.define";

    private final List<BaseEnhancePluginDefine> signatureMatchDefine = Lists.newArrayList();

    private final Map<String, LinkedList<BaseEnhancePluginDefine>> nameMatchDefine = Maps.newHashMap();

    public void loadPlugins() throws Exception {
        //类加载器初始化
        AgentClassLoader.initDefaultLoader();
        //命令类初始化
        CommandSupport.build();
        //扫描包
        Set<Class<?>> classes = getClasses(PACKAGE_PATH, AgentClassLoader.getDefault());
        if (classes == null || classes.size() == 0) {
            return;
        }
        //初始化插件类
        for (Class<?> clazz : classes) {
            //判断是否为默认类
            if (clazz.equals(CmdEnhancePluginDefine.class) || clazz.equals(DefaultEnhancePluginDefine.class) || clazz.equals(BaseEnhancePluginDefine.class)) {
                continue;
            }
            //插件类继承基础类
            if (this.isExtendBaseEnhancePluginDefine(clazz)) {
                BaseEnhancePluginDefine plugin = (BaseEnhancePluginDefine) clazz.newInstance();
                //获取匹配逻辑
                ClassMatch match = plugin.enhanceClass();
                if (match == null) {
                    continue;
                }

                if (match instanceof NameMatch) {
                    NameMatch nameMatch = (NameMatch) match;
                    LinkedList<BaseEnhancePluginDefine> defines = nameMatchDefine.get(nameMatch.getClassName());
                    if (defines == null) {
                        defines = Lists.newLinkedList();
                        nameMatchDefine.put(nameMatch.getClassName(), defines);
                    }
                    defines.add(plugin);
                } else {
                    signatureMatchDefine.add(plugin);
                }
            }
        }
    }

    /**
     * 获取插件列表
     *
     * @param typeDescription
     * @return
     */
    public List<BaseEnhancePluginDefine> find(TypeDescription typeDescription) {
        List<BaseEnhancePluginDefine> matchedPlugins = Lists.newLinkedList();
        String typeName = typeDescription.getTypeName();
        if (nameMatchDefine.containsKey(typeName)) {
            matchedPlugins.addAll(nameMatchDefine.get(typeName));
        }

        for (BaseEnhancePluginDefine pluginDefine : signatureMatchDefine) {
            IndirectMatch match = (IndirectMatch) pluginDefine.enhanceClass();
            if (match.isMatch(typeDescription)) {
                matchedPlugins.add(pluginDefine);
            }
        }

        return matchedPlugins;
    }

    public List<BaseEnhancePluginDefine> getSignatureMatchDefine() {
        return signatureMatchDefine;
    }

    public Map<String, LinkedList<BaseEnhancePluginDefine>> getNameMatchDefine() {
        return nameMatchDefine;
    }

    /**
     * 是否继承基类
     *
     * @param clazz
     * @return
     */
    private boolean isExtendBaseEnhancePluginDefine(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null) {
            return false;
        }

        if (superclass.equals(Object.class)) {
            return false;
        }

        if (superclass.equals(BaseEnhancePluginDefine.class)) {
            return true;
        }

        return this.isExtendBaseEnhancePluginDefine(superclass);
    }

    /**
     * 根据包名获取包下面所有的类名
     *
     * @param pack
     * @return
     * @throws Exception
     */
    public Set<Class<?>> getClasses(String pack, ClassLoader classLoader) throws Exception {
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<>();
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        // 循环迭代下去
        while (dirs.hasMoreElements()) {
            // 获取下一个元素
            URL url = dirs.nextElement();
            // 得到协议的名称
            String protocol = url.getProtocol();
            // 如果是以文件的形式保存在服务器上
            if ("file".equals(protocol)) {
                // 获取包的物理路径
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                // 以文件的方式扫描整个包下的文件 并添加到集合中
                findClassesInPackageByFile(packageName, filePath, true, classes, classLoader);
            } else if ("jar".equals(protocol)) {
                // 如果是jar包文件
                JarFile jar;
                // 获取jar
                jar = ((JarURLConnection) url.openConnection()).getJarFile();
                // 从此jar包 得到一个枚举类
                Enumeration<JarEntry> entries = jar.entries();
                findClassesInPackageByJar(packageName, entries, packageDirName, true, classes, classLoader);
            }
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     */
    private void findClassesInPackageByFile(String packageName, String packagePath, boolean recursive, Set<Class<?>> classes, ClassLoader classLoader) throws ClassNotFoundException {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        // 循环所有文件
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes, classLoader);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = classLoader.loadClass(packageName + '.' + className);
                if (!clazz.isMemberClass() && !clazz.isAnonymousClass() && !Modifier.isAbstract(clazz.getModifiers())) {
                    classes.add(clazz);
                }
            }
        }
    }

    /**
     * 以jar的形式来获取包下的所有Class
     */
    private void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, boolean recursive, Set<Class<?>> classes, ClassLoader classLoader) throws ClassNotFoundException {
        // 同样的进行循环迭代
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if ((idx != -1) || recursive) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        Class<?> clazz = classLoader.loadClass(packageName + '.' + className);
                        if (!clazz.isMemberClass() && !clazz.isAnonymousClass() && !Modifier.isAbstract(clazz.getModifiers())) {
                            // 添加到classes
                            classes.add(clazz);
                        }
                    }
                }
            }
        }
    }
}