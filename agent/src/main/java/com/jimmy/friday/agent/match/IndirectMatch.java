package com.jimmy.friday.agent.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface IndirectMatch extends ClassMatch {
    ElementMatcher.Junction buildJunction();

    boolean isMatch(TypeDescription typeDescription);
}
