package com.jimmy.friday.framework.other.schedule;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

public class ScheduleImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> importClasses = new ArrayList<>();
        importClasses.add("com.jimmy.friday.framework.config.ScheduleConfig");

        return importClasses.toArray(new String[0]);
    }
}
