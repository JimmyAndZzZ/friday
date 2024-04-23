package com.jimmy.friday.agent;

import com.google.common.collect.Sets;
import com.jimmy.friday.agent.base.ConstructsInterceptPoint;
import com.jimmy.friday.agent.base.MethodsInterceptPoint;
import com.jimmy.friday.agent.base.StaticMethodsInterceptPoint;
import com.jimmy.friday.agent.bytebuddy.*;
import com.jimmy.friday.agent.bytebuddy.support.WitnessFinder;
import com.jimmy.friday.agent.bytebuddy.support.WitnessMethod;
import com.jimmy.friday.agent.core.*;
import com.jimmy.friday.agent.enums.ClassCacheMode;
import com.jimmy.friday.agent.match.ClassMatch;
import com.jimmy.friday.agent.match.IndirectMatch;
import com.jimmy.friday.agent.match.ProtectiveShieldMatcher;
import com.jimmy.friday.agent.plugin.define.BaseEnhancePluginDefine;
import com.jimmy.friday.agent.plugin.define.base.DefaultEnhancePluginDefine;
import com.jimmy.friday.agent.support.QpsSupport;
import com.jimmy.friday.agent.support.TraceSupport;
import com.jimmy.friday.boot.other.ConfigConstants;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.bytebuddy.jar.asm.Opcodes.ACC_PRIVATE;
import static net.bytebuddy.jar.asm.Opcodes.ACC_VOLATILE;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class AgentPremain {

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        PluginBootstrap pluginBootstrap = new PluginBootstrap();
        //判断配置文件是否存在
        if (!ConfigLoad.getDefault().isExistProperties()) {
            return;
        }
        //读取插件
        pluginBootstrap.loadPlugins();
        //激活收集传输
        TraceSupport.build();
        //激活qps收集
        QpsSupport.build();
        //关闭钩子注册
        DestroyHook.shutdownHook();
        //动态构建操作，根据transformer规则执行拦截操作
        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
            String typeName = typeDescription.getTypeName();

            //获取插件列表
            List<BaseEnhancePluginDefine> pluginDefines = pluginBootstrap.find(typeDescription);
            //增强上下文
            EnhanceContext context = new EnhanceContext();

            if (isMatch(typeDescription)) {
                MethodList<MethodDescription.InDefinedShape> declaredMethods = typeDescription.getDeclaredMethods();

                Set<String> collect = Sets.newHashSet();
                for (MethodDescription.InDefinedShape declaredMethod : declaredMethods) {
                    String internalName = declaredMethod.getInternalName();

                    if (internalName.equalsIgnoreCase("<init>")) {
                        continue;
                    }

                    if (declaredMethod.isStatic()) {
                        continue;
                    }

                    AnnotationList declaredAnnotations = declaredMethod.getDeclaredAnnotations();
                    if (declaredAnnotations != null && !declaredAnnotations.isEmpty()) {
                        for (AnnotationDescription declaredAnnotation : declaredAnnotations) {
                            TypeDescription annotationType = declaredAnnotation.getAnnotationType();

                            if (annotationType.getTypeName().equalsIgnoreCase("com.jimmy.friday.boot.annotations.Trace")) {
                                AnnotationLoad.putTracePoint(typeName, internalName);
                            }

                            if (annotationType.getTypeName().equalsIgnoreCase("com.jimmy.friday.boot.annotations.QpsMessage")) {
                                AnnotationLoad.putQpsPoint(typeName, internalName);
                            }
                        }
                    }

                    collect.add(internalName);
                }

                if (collect.isEmpty()) {
                    return builder;
                }

                pluginDefines.add(new DefaultEnhancePluginDefine(typeName, collect));
            }
            //插件初始化
            for (BaseEnhancePluginDefine pluginDefine : pluginDefines) {
                DynamicType.Builder<?> possibleNewBuilder = instanceDefine(typeDescription, pluginDefine, builder, classLoader, context);
                context.initializationStageCompleted();
                if (possibleNewBuilder != null) {
                    builder = possibleNewBuilder;
                }
            }
            //构建拦截规则
            return builder;
        };
        //采用Byte Buddy的AgentBuilder结合Java Agent处理程序
        AgentBuilder agentBuilder = new AgentBuilder
                //采用ByteBuddy作为默认的Agent实例
                .Default()
                //忽略系统方法
                .ignore(nameStartsWith("net.bytebuddy.").or(nameStartsWith("org.slf4j.")).or(nameStartsWith("org.groovy.")).or(nameContains("javassist")).or(nameContains("sun.usagetracker")).or(nameContains("sun.misc")).or(nameContains(".asm.")).or(nameContains(".reflectasm.")).or(nameStartsWith("sun.reflect")).or(allAgentExcludeToolkit()).or(isSynthetic()));
        //缓存方式
        agentBuilder = agentBuilder.with(new CacheableTransformerDecorator(ClassCacheMode.MEMORY));
        //拦截匹配方式
        agentBuilder.type(buildMatch(pluginBootstrap))
                //拦截到的类由transformer处理
                .transform(transformer).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).with(new RedefinitionListener()).with(new Listener())
                //安装到 Instrumentation
                .installOn(inst);
    }

    /**
     * 是否忽略
     *
     * @param typeDescription
     * @return
     */
    private static boolean isMatch(TypeDescription typeDescription) {
        String typeName = typeDescription.getTypeName();
        AnnotationList declaredAnnotations = typeDescription.getDeclaredAnnotations();

        if (typeName.contains("BySpringCGLIB$$")) {
            return false;
        }

        if (typeName.contains("$original$")) {
            return false;
        }

        if (declaredAnnotations != null && !declaredAnnotations.isEmpty()) {
            for (AnnotationDescription declaredAnnotation : declaredAnnotations) {
                TypeDescription annotationType = declaredAnnotation.getAnnotationType();
                if (annotationType.getTypeName().equalsIgnoreCase("com.baomidou.mybatisplus.annotation.TableName")) {
                    return false;
                }

                if (annotationType.getTypeName().equalsIgnoreCase("javax.persistence.Table")) {
                    return false;
                }

                if (annotationType.getTypeName().equalsIgnoreCase("com.jimmy.friday.boot.annotations.Ignore")) {
                    return false;
                }
            }
        }

        return ConfigLoad.getDefault().isMatch(typeName);
    }

    /**
     * 构建匹配条件
     *
     * @param pluginBootstrap
     * @return
     */
    private static ElementMatcher<? super TypeDescription> buildMatch(PluginBootstrap pluginBootstrap) {
        ElementMatcher.Junction judge = new ElementMatcher.Junction<NamedElement>() {
            @Override
            public <U extends NamedElement> Junction<U> and(ElementMatcher<? super U> other) {
                return new Conjunction<>(this, other);
            }

            @Override
            public <U extends NamedElement> Junction<U> or(ElementMatcher<? super U> other) {
                return new Conjunction<>(this, other);
            }

            @Override
            public boolean matches(NamedElement target) {
                String actualName = target.getActualName();

                if (pluginBootstrap.getNameMatchDefine().containsKey(actualName)) {
                    return true;
                }

                return ConfigLoad.getDefault().isMatch(actualName);
            }
        };
        //非接口
        judge = judge.and(not(isInterface()));
        judge = judge.and(not(isAnnotation()));
        judge = judge.and(not(isAbstract()));
        judge = judge.and(not(isEnum()));
        List<BaseEnhancePluginDefine> signatureMatchDefine = pluginBootstrap.getSignatureMatchDefine();
        if (signatureMatchDefine != null && signatureMatchDefine.size() > 0) {
            for (BaseEnhancePluginDefine define : signatureMatchDefine) {
                ClassMatch match = define.enhanceClass();
                if (match instanceof IndirectMatch) {
                    judge = judge.or(((IndirectMatch) match).buildJunction());
                }
            }
        }

        return new ProtectiveShieldMatcher(judge);
    }

    /**
     * 剔除
     *
     * @return
     */
    private static ElementMatcher.Junction<NamedElement> allAgentExcludeToolkit() {
        return nameStartsWith("com.jimmy.friday.agent.");
    }

    /**
     * 实例定义
     *
     * @param pluginDefine
     * @param builder
     * @return
     */
    private static DynamicType.Builder<?> instanceDefine(TypeDescription typeDescription, BaseEnhancePluginDefine pluginDefine, DynamicType.Builder<?> builder, ClassLoader classLoader, EnhanceContext enhanceContext) {
        WitnessFinder finder = WitnessFinder.INSTANCE;

        String[] witnessClasses = pluginDefine.witnessClasses();
        List<WitnessMethod> witnessMethods = pluginDefine.witnessMethods();
        ConstructsInterceptPoint[] constructsInterceptPoints = pluginDefine.getConstructsInterceptPoints();
        MethodsInterceptPoint[] instanceMethodsInterceptPoints = pluginDefine.getInstanceMethodsInterceptPoints();
        StaticMethodsInterceptPoint[] staticMethodsInterceptPoints = pluginDefine.getStaticMethodsInterceptPoints();
        //验证类是否存在
        if (witnessClasses != null) {
            for (String witnessClass : witnessClasses) {
                if (!finder.exist(witnessClass, classLoader)) {
                    return null;
                }
            }
        }
        //验证方法是否存在
        if (witnessMethods != null && !witnessMethods.isEmpty()) {
            for (WitnessMethod witnessMethod : witnessMethods) {
                if (!finder.exist(witnessMethod, classLoader)) {
                    return null;
                }
            }
        }
        //静态方法拦截
        if (staticMethodsInterceptPoints != null && staticMethodsInterceptPoints.length > 0) {
            for (StaticMethodsInterceptPoint staticMethodsInterceptPoint : staticMethodsInterceptPoints) {
                boolean overrideArgs = staticMethodsInterceptPoint.isOverrideArgs();
                //判断是否需要覆盖参数
                if (overrideArgs) {
                    builder = builder.method(isStatic().and(staticMethodsInterceptPoint.getMethodsMatcher())).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideCallable.class)).to(new StaticOverrideCallableMethodAdviceInterceptor(staticMethodsInterceptPoint.getMethodsAround(), classLoader)));
                } else {
                    builder = builder.method(isStatic().and(staticMethodsInterceptPoint.getMethodsMatcher())).intercept(MethodDelegation.withDefaultConfiguration().to(new StaticMethodAdviceInterceptor(staticMethodsInterceptPoint.getMethodsAround(), classLoader)));
                }
            }
        }

        boolean existedConstructorInterceptPoint = false;
        if (constructsInterceptPoints != null && constructsInterceptPoints.length > 0) {
            existedConstructorInterceptPoint = true;
        }
        boolean existedMethodsInterceptPoints = false;
        if (instanceMethodsInterceptPoints != null && instanceMethodsInterceptPoints.length > 0) {
            existedMethodsInterceptPoints = true;
        }
        //不存在方法增强和构造方法增强就返回
        if (!existedConstructorInterceptPoint && !existedMethodsInterceptPoints) {
            return builder;
        }
        //类增强
        if (!typeDescription.isAssignableTo(EnhancedInstance.class)) {
            if (!enhanceContext.isObjectExtended()) {
                builder = builder.defineField(ConfigConstants.CONTEXT_ATTR_NAME, EnhancedField.class, ACC_PRIVATE | ACC_VOLATILE).implement(EnhancedInstance.class).intercept(FieldAccessor.ofField(ConfigConstants.CONTEXT_ATTR_NAME));
                enhanceContext.extendObjectCompleted();
            }
        }
        //构造器拦截
        if (existedConstructorInterceptPoint) {
            for (ConstructsInterceptPoint constructsInterceptPoint : constructsInterceptPoints) {
                builder = builder.constructor(constructsInterceptPoint.getConstructorMatcher()).intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(new ConstructAdviceInterceptor(constructsInterceptPoint.getConstructorAround(), classLoader))));
            }
        }
        //方法拦截
        if (existedMethodsInterceptPoints) {
            for (MethodsInterceptPoint methodsInterceptPoint : instanceMethodsInterceptPoints) {
                boolean overrideArgs = methodsInterceptPoint.isOverrideArgs();

                ElementMatcher.Junction<MethodDescription> junction = not(isStatic()).and(methodsInterceptPoint.getMethodsMatcher());
                //判断是否需要覆盖参数
                if (overrideArgs) {
                    builder = builder.method(junction).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideCallable.class)).to(new OverrideCallableMethodAdviceInterceptor(methodsInterceptPoint.getMethodsAround(), classLoader)));
                } else {
                    builder = builder.method(junction).intercept(MethodDelegation.withDefaultConfiguration().to(new MethodAdviceInterceptor(methodsInterceptPoint.getMethodsAround(), classLoader)));
                }
            }
        }

        return builder;
    }

    private static class Listener implements AgentBuilder.Listener {
        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

        }

        @Override
        public void onTransformation(final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module, final boolean loaded, final DynamicType dynamicType) {
        }

        @Override
        public void onIgnored(final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module, final boolean loaded) {

        }

        @Override
        public void onError(final String typeName, final ClassLoader classLoader, final JavaModule module, final boolean loaded, final Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        }
    }

    private static class RedefinitionListener implements AgentBuilder.RedefinitionStrategy.Listener {

        @Override
        public void onBatch(int index, List<Class<?>> batch, List<Class<?>> types) {
            /* do nothing */
        }

        @Override
        public Iterable<? extends List<Class<?>>> onError(int index, List<Class<?>> batch, Throwable throwable, List<Class<?>> types) {
            throwable.printStackTrace();
            return Collections.emptyList();
        }

        @Override
        public void onComplete(int amount, List<Class<?>> types, Map<List<Class<?>>, Throwable> failures) {
            /* do nothing */
        }
    }
}
