package com.jimmy.friday.framework.other.transaction;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

public class TransactionImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> importClasses = new ArrayList<>();
        importClasses.add("com.jimmy.friday.framework.config.TransactionConfig");

        return importClasses.toArray(new String[0]);
    }
}
