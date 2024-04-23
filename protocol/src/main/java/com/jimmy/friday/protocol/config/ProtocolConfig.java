package com.jimmy.friday.protocol.config;

import com.jimmy.friday.protocol.core.ProtocolFactory;
import com.jimmy.friday.protocol.registered.RegisteredInit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ProtocolProperties.class)
public class ProtocolConfig {

    @Bean
    public ProtocolFactory protocolFactory() {
        return new ProtocolFactory();
    }

    @Bean
    public RegisteredInit registeredInit(@Qualifier("protocolFactory") ProtocolFactory protocolFactory) throws Exception {
        protocolFactory.registeredInit();
        return new RegisteredInit();
    }
}
