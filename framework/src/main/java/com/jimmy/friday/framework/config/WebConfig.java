package com.jimmy.friday.framework.config;

import com.jimmy.friday.framework.condition.HttpCondition;
import com.jimmy.friday.framework.other.GatewayWebInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Conditional(value = HttpCondition.class)
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public GatewayWebInterceptor gatewayWebInterceptor() {
        return new GatewayWebInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.gatewayWebInterceptor()).addPathPatterns("/**");
    }
}
