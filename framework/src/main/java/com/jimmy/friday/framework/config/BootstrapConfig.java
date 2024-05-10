package com.jimmy.friday.framework.config;

import com.jimmy.friday.framework.Boot;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.core.DestroyHook;
import com.jimmy.friday.framework.core.GlobalCache;
import com.jimmy.friday.framework.process.AckProcess;
import com.jimmy.friday.framework.support.TransmitSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootstrapConfig {

    @Bean
    @ConditionalOnMissingBean
    public AckProcess ackProcess(ApplicationContext applicationContext) {
        return new AckProcess(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigLoad configLoad() {
        return new ConfigLoad();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalCache globalCache() {
        return new GlobalCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransmitSupport transmitSupport(ConfigLoad configLoad) {
        return new TransmitSupport(configLoad);
    }

    @Bean(destroyMethod = "showdown")
    @ConditionalOnMissingBean
    public DestroyHook destroyHook(ConfigLoad configLoad, TransmitSupport transmitSupport, ApplicationContext applicationContext) {
        return new DestroyHook(configLoad, transmitSupport, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public Boot boot(TransmitSupport transmitSupport) {
        return new Boot(transmitSupport);
    }
}
