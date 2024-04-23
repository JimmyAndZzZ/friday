package com.jimmy.friday.framework.config;

import com.jimmy.friday.framework.callback.GatewayCallback;
import com.jimmy.friday.framework.bootstrap.GatewayBootstrap;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.process.*;
import com.jimmy.friday.framework.support.*;
import com.jimmy.friday.framework.process.*;
import com.jimmy.friday.framework.support.*;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.classreading.MetadataReaderFactory;

@Configuration
public class GatewayConfig {

    @Bean
    public RegisterSupport registerSupport(TransmitSupport transmitSupport, ConfigLoad configLoad, DefaultListableBeanFactory beanFactory, MetadataReaderFactory metadataReaderFactory, RpcProtocolInvokeProcess rpcProtocolInvokeProcess) {
        return new RegisterSupport(transmitSupport, configLoad, beanFactory, metadataReaderFactory, rpcProtocolInvokeProcess);
    }

    @Bean
    public CallbackSupport callbackSupport() {
        return new CallbackSupport();
    }

    @Bean
    public InvokeSupport invokeSupport() {
        return new InvokeSupport();
    }

    @Bean
    public GatewayCallback gatewayCallback(RegisterSupport registerSupport, ConfigLoad configLoad, TransmitSupport transmitSupport, ChannelSupport channelSupport) {
        return new GatewayCallback(registerSupport, configLoad, transmitSupport, channelSupport);
    }

    @Bean
    public GatewayBootstrap gatewayBootstrap(RegisterSupport registerSupport, ConfigLoad configLoad) {
        return new GatewayBootstrap(registerSupport, configLoad, invokeSupport());
    }

    @Bean
    public ChannelSupport channelSupport(ConfigLoad configLoad, TransmitSupport transmitSupport) {
        return new ChannelSupport(configLoad, transmitSupport);
    }

    @Configuration
    protected static class ProcessConfig {

        @Bean
        public HeartbeatProcess heartbeatProcess() {
            return new HeartbeatProcess();
        }

        @Bean
        public GatewayInvokeProcess gatewayInvokeProcess() {
            return new GatewayInvokeProcess();
        }

        @Bean
        public RpcProtocolInvokeProcess rpcProtocolInvokeProcess(TransmitSupport transmitSupport) {
            return new RpcProtocolInvokeProcess(transmitSupport);
        }

        @Bean
        public InvokeCallbackProcess invokeCallbackProcess(CallbackSupport callbackSupport) {
            return new InvokeCallbackProcess(callbackSupport);
        }

        @Bean
        public ChannelReceiveProcess channelReceiveProcess(ChannelSupport channelSupport) {
            return new ChannelReceiveProcess(channelSupport);
        }

        @Bean
        public ChannelAckProcess channelAckProcess(ChannelSupport channelSupport) {
            return new ChannelAckProcess(channelSupport);
        }
    }
}
