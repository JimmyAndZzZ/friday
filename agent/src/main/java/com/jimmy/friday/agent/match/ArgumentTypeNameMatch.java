
package com.jimmy.friday.agent.match;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.matcher.ElementMatcher;

public class ArgumentTypeNameMatch implements ElementMatcher<MethodDescription> {
    private int index;

    private String argumentTypeName;

    private ArgumentTypeNameMatch(int index, String argumentTypeName) {
        this.index = index;
        this.argumentTypeName = argumentTypeName;
    }

    @Override
    public boolean matches(MethodDescription target) {
        ParameterList<?> parameters = target.getParameters();
        if (parameters.size() > index) {
            return parameters.get(index).getType().asErasure().getName().equals(argumentTypeName);
        }

        return false;
    }

    public static ElementMatcher<MethodDescription> takesArgumentWithType(int index, String argumentTypeName) {
        return new ArgumentTypeNameMatch(index, argumentTypeName);
    }
}
