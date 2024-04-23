package com.jimmy.friday.agent.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class HierarchyMatch implements IndirectMatch {
    private String[] parentTypes;

    private HierarchyMatch(String[] parentTypes) {
        if (parentTypes == null || parentTypes.length == 0) {
            throw new IllegalArgumentException("parentTypes is null");
        }
        this.parentTypes = parentTypes;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;
        for (String superTypeName : parentTypes) {
            if (junction == null) {
                junction = buildSuperClassMatcher(superTypeName);
            } else {
                junction = junction.and(buildSuperClassMatcher(superTypeName));
            }
        }
        junction = junction.and(not(isInterface()));
        return junction;
    }

    private ElementMatcher.Junction buildSuperClassMatcher(String superTypeName) {
        return hasSuperType(named(superTypeName));
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        List<String> parentTypes = new ArrayList<String>(Arrays.asList(this.parentTypes));

        TypeList.Generic implInterfaces = typeDescription.getInterfaces();
        for (TypeDescription.Generic implInterface : implInterfaces) {
            matchHierarchyClass(implInterface, parentTypes);
        }

        if (typeDescription.getSuperClass() != null) {
            matchHierarchyClass(typeDescription.getSuperClass(), parentTypes);
        }

        return parentTypes.size() == 0;

    }

    private void matchHierarchyClass(TypeDescription.Generic clazz, List<String> parentTypes) {
        parentTypes.remove(clazz.asRawType().getTypeName());
        if (parentTypes.size() == 0) {
            return;
        }

        for (TypeDescription.Generic generic : clazz.getInterfaces()) {
            matchHierarchyClass(generic, parentTypes);
        }

        TypeDescription.Generic superClazz = clazz.getSuperClass();
        if (superClazz != null && !clazz.getTypeName().equals("java.lang.Object")) {
            matchHierarchyClass(superClazz, parentTypes);
        }

    }

    public static IndirectMatch byHierarchyMatch(String... parentTypes) {
        return new HierarchyMatch(parentTypes);
    }
}
