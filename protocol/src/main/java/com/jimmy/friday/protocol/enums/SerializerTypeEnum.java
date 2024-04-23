package com.jimmy.friday.protocol.enums;

import com.jimmy.friday.protocol.serializer.Base64Serializer;
import com.jimmy.friday.protocol.base.Serializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SerializerTypeEnum {

    DEFAULT("x-java-serialized-default", new Serializer() {
        @Override
        public String serialize(String text) {
            return text;
        }

        @Override
        public String deserialize(String text) {
            return text;
        }
    }),

    BASE64("x-java-serialized-base64", new Base64Serializer());

    private String type;

    private Serializer serializer;

    public static SerializerTypeEnum queryByType(String type) {
        for (SerializerTypeEnum value : SerializerTypeEnum.values()) {
            if (value.type.equalsIgnoreCase(type)) {
                return value;
            }
        }

        return null;
    }
}
