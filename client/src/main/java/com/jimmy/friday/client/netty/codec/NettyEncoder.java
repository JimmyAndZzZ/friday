package com.jimmy.friday.client.netty.codec;

import com.jimmy.friday.client.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder {
    private final Class<?> genericClass;

    public NettyEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    /**
     * 将对象转换为字节码然后写入到 ByteBuf 对象中
     *
     * @param channelHandlerContext 解码器关联的 ChannelHandlerContext 对象
     * @param o                     编码前的业务对象
     * @param byteBuf               存储编码后的字节数组
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            String jsonString = JsonUtil.toString(o); // 使用 FastJSON 序列化对象为 JSON 字符串
            byte[] bytes = jsonString.getBytes(); // 获取 JSON 字符串的字节数组
            byteBuf.writeBytes(bytes); // 将字节数组写入 ByteBu
        }
    }
}
