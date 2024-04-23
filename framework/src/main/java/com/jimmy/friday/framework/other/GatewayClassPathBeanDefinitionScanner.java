package com.jimmy.friday.framework.other;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;

public class GatewayClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

    public GatewayClassPathBeanDefinitionScanner(boolean useDefaultFilters) {
        super(useDefaultFilters);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return (metadata.isIndependent() && (metadata.isConcrete() || (metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName()))) || metadata.isInterface());
    }
}
