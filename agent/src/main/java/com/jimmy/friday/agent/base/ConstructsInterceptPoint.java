package com.jimmy.friday.agent.base;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface ConstructsInterceptPoint {

    ElementMatcher<MethodDescription> getConstructorMatcher();

    String getConstructorAround();
}
