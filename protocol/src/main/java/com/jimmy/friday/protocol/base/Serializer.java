package com.jimmy.friday.protocol.base;

/**
 * 序列化接口
 *
 * @author damon
 */
public interface Serializer {

    String serialize(String text);

    String deserialize(String text);
}
