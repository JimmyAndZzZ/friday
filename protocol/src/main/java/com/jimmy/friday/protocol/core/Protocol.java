package com.jimmy.friday.protocol.core;

import com.jimmy.friday.protocol.annotations.Listen;
import com.jimmy.friday.protocol.enums.SerializerTypeEnum;
import lombok.Data;

@Data
public class Protocol {

    private String topic;

    private Boolean isAck = true;

    private String groupId;

    private Integer port;

    private Integer concurrentConsumers = 10;

    private Integer maxConcurrentConsumers = 20;

    private Integer batchSize = 1000;

    private Boolean isWsServerToClient = false;

    private SerializerTypeEnum serializerType;

    public static Protocol buildOutput(com.jimmy.friday.protocol.annotations.Protocol annotation) {
        Protocol protocol = new Protocol();
        protocol.setTopic(annotation.topic());
        protocol.setGroupId(annotation.groupId());
        protocol.setPort(annotation.port());
        protocol.setSerializerType(annotation.serializerType());
        return protocol;
    }

    public static Protocol buildInput(Listen listen) {
        boolean ack = listen.isAck();
        com.jimmy.friday.protocol.annotations.Protocol annotation = listen.protocol();
        String topic = annotation.topic();
        String groupId = annotation.groupId();

        Protocol protocolInfo = new Protocol();
        protocolInfo.setTopic(topic);
        protocolInfo.setIsAck(ack);
        protocolInfo.setGroupId(groupId);
        protocolInfo.setPort(annotation.port());
        protocolInfo.setConcurrentConsumers(annotation.concurrentConsumers());
        protocolInfo.setMaxConcurrentConsumers(annotation.maxConcurrentConsumers());
        protocolInfo.setBatchSize(annotation.batchSize());
        return protocolInfo;
    }
}
