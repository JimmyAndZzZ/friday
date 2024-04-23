package com.jimmy.friday.agent.match.logical;


import com.jimmy.friday.agent.match.IndirectMatch;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Match classes by multiple criteria with OR conjunction
 */
public class LogicalOrMatch implements IndirectMatch {
    private final IndirectMatch[] indirectMatches;

    /**
     * Don't instantiate this class directly, use {@link LogicalMatchOperation} instead
     *
     * @param indirectMatches the matching criteria to conjunct with OR
     */
    LogicalOrMatch(final IndirectMatch... indirectMatches) {
        this.indirectMatches = indirectMatches;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;

        for (final IndirectMatch indirectMatch : indirectMatches) {
            if (junction == null) {
                junction = indirectMatch.buildJunction();
            } else {
                junction = junction.or(indirectMatch.buildJunction());
            }
        }

        return junction;
    }

    @Override
    public boolean isMatch(final TypeDescription typeDescription) {
        for (final IndirectMatch indirectMatch : indirectMatches) {
            if (indirectMatch.isMatch(typeDescription)) {
                return true;
            }
        }

        return false;
    }

}
