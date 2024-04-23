package com.jimmy.friday.agent.match;

import com.google.common.base.Strings;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.regex.Pattern;

public class MethodNameMatch implements ElementMatcher<MethodDescription> {

    private String expression;

    public MethodNameMatch(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean matches(MethodDescription target) {
        if (Strings.isNullOrEmpty(expression)) {
            return false;
        }

        String name = target.getName();
        if (name.equalsIgnoreCase(expression)) {
            return true;
        }

        return Pattern.matches(expression, name);
    }

    public static ElementMatcher<MethodDescription> expressionBuild(String expression) {
        return new MethodNameMatch(expression);
    }
}
