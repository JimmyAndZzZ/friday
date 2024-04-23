package com.jimmy.friday.center.config;

import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "friday.center")
public class FridayConfigProperties {

    private Integer serverPort = 11211;

    private String tempPath;

    private String kafkaServer;

    private String remindUrl;

    private ProtocolEnum protocolType = ProtocolEnum.MEMORY;

    private ProtocolProperty protocol = new ProtocolProperty();
}
