package com.jimmy.friday.agent.match;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class AllMethodMatch implements ElementMatcher<MethodDescription> {

    private static AllMethodMatch single;

    private AllMethodMatch() {

    }

    @Override
    public boolean matches(MethodDescription target) {
        return true;
    }

    public static ElementMatcher<MethodDescription> build() {
        if (single == null) {
            synchronized (AllMethodMatch.class) {
                if (single == null) {
                    single = new AllMethodMatch();
                }

            }
        }
        return single;
    }
}
