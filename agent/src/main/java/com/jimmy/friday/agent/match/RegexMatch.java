package com.jimmy.friday.agent.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.nameMatches;

public class RegexMatch implements IndirectMatch {
    private String[] regexExpressions;

    private RegexMatch(String... regexExpressions) {
        if (regexExpressions == null || regexExpressions.length == 0) {
            throw new IllegalArgumentException("annotations is null");
        }
        this.regexExpressions = regexExpressions;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction regexJunction = null;
        for (String regexExpression : regexExpressions) {
            if (regexJunction == null) {
                regexJunction = nameMatches(regexExpression);
            } else {
                regexJunction = regexJunction.or(nameMatches(regexExpression));
            }
        }
        return regexJunction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        boolean isMatch = false;
        for (String matchExpression : regexExpressions) {
            isMatch = typeDescription.getTypeName().matches(matchExpression);
            if (isMatch) {
                break;
            }
        }
        return isMatch;
    }

    public static RegexMatch byRegexMatch(String... regexExpressions) {
        return new RegexMatch(regexExpressions);
    }
}