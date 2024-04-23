package com.jimmy.friday.agent.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Match class with a given set of classes.
 */
public class MultiClassNameMatch implements IndirectMatch {

    private List<String> matchClassNames;

    private MultiClassNameMatch(String[] classNames) {
        if (classNames == null || classNames.length == 0) {
            throw new IllegalArgumentException("match class names is null");
        }
        this.matchClassNames = Arrays.asList(classNames);
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;
        for (String name : matchClassNames) {
            if (junction == null) {
                junction = named(name);
            } else {
                junction = junction.or(named(name));
            }
        }
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        return matchClassNames.contains(typeDescription.getTypeName());
    }

    public static IndirectMatch byMultiClassMatch(String... classNames) {
        return new MultiClassNameMatch(classNames);
    }
}
