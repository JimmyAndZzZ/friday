package com.jimmy.friday.protocol.config;

import com.google.common.collect.Maps;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "gateway.protocol")
public class ProtocolProperties {

    private Map<ProtocolEnum, ProtocolProperty> protocols = Maps.newHashMap();

    @PostConstruct
    public void init() {
        protocols.put(ProtocolEnum.WEBSOCKET, new ProtocolProperty());
        protocols.put(ProtocolEnum.MEMORY, new ProtocolProperty());
    }
}
