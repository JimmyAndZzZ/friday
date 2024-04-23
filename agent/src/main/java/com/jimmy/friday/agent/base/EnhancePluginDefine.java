package com.jimmy.friday.agent.base;

import com.jimmy.friday.agent.match.ClassMatch;

public interface EnhancePluginDefine {

    MethodsInterceptPoint[] getInstanceMethodsInterceptPoints();

    StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints();

    ClassMatch enhanceClass();

    ConstructsInterceptPoint[] getConstructsInterceptPoints();
}
