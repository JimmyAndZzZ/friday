package com.jimmy.friday.protocol.serializer;

import cn.hutool.core.codec.Base64;
import com.jimmy.friday.protocol.exception.ProtocolException;
import com.jimmy.friday.protocol.base.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

@Slf4j
public class Base64Serializer implements Serializer {

    @Override
    public String serialize(String text) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(out)) {
                deflaterOutputStream.write(text.getBytes(StandardCharsets.UTF_8));
            }
            return Base64.encode(out.toByteArray());
        } catch (Exception e) {
            throw new ProtocolException(e.getMessage());
        }
    }

    @Override
    public String deserialize(String text) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (OutputStream outputStream = new InflaterOutputStream(os)) {
                outputStream.write(Base64.decode(text));
            }
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage());
        }
    }
}
