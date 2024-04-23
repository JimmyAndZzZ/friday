package com.jimmy.friday.agent.match.logical;

import com.jimmy.friday.agent.match.IndirectMatch;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NegatingMatcher;

public class LogicalMatchOperation {
    public static IndirectMatch and(final IndirectMatch... matches) {
        return new LogicalAndMatch(matches);
    }

    public static IndirectMatch or(final IndirectMatch... matches) {
        return new LogicalOrMatch(matches);
    }

    public static IndirectMatch not(final IndirectMatch match) {
        return new IndirectMatch() {
            @Override
            public ElementMatcher.Junction buildJunction() {
                return new NegatingMatcher(match.buildJunction());
            }

            @Override
            public boolean isMatch(final TypeDescription typeDescription) {
                return !match.isMatch(typeDescription);
            }
        };
    }
}
