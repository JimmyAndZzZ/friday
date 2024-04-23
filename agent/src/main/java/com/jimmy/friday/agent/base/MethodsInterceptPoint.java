package com.jimmy.friday.agent.base;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface MethodsInterceptPoint {

    ElementMatcher<MethodDescription> getMethodsMatcher();

    String getMethodsAround();

    //@SuperCall 注解注入的 Callable 参数来调用目标方法时，是无法动态修改参数的，如果想要动态修改参数，则需要用到 @Morph 注解以及一些绑定操作,
    boolean isOverrideArgs();
}
