package com.jimmy.friday.agent.bytebuddy.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

@ToString
@RequiredArgsConstructor
public class WitnessMethod {

    @Getter
    private final String declaringClassName;

    @Getter
    private final ElementMatcher<? super MethodDescription.InDefinedShape> elementMatcher;

}
