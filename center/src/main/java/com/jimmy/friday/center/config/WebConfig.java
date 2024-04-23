package com.jimmy.friday.center.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

@Configuration
public class WebConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement(FridayConfigProperties configProperties) {
        MultipartConfigFactory multipartConfigFactory = new MultipartConfigFactory();
        multipartConfigFactory.setLocation(configProperties.getTempPath());
        return multipartConfigFactory.createMultipartConfig();
    }
}
