package com.jimmy.friday.protocol.registered;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.protocol.condition.RedisCondition;
import com.jimmy.friday.protocol.annotations.RegisteredType;
import com.jimmy.friday.protocol.base.Input;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;

import java.util.Map;

@Slf4j
@RegisteredType(type = ProtocolEnum.REDIS, condition = RedisCondition.class)
public class RedisRegistered extends BaseRegistered {

    private RedissonClient client;

    private Map<String, MessageListener> listenerMap = Maps.newHashMap();

    @Override
    public void close(Protocol info) {
        RTopic topic = client.getTopic(info.getTopic(), new SerializationCodec());
        MessageListener messageListener = listenerMap.get(info.getTopic());
        if (messageListener != null) {
            topic.removeListener(messageListener);
            listenerMap.remove(info.getTopic());
        }
    }

    @Override
    public void init(ProtocolProperty protocolProperty) {
        if (protocolProperty == null) {
            throw new IllegalArgumentException("未配置Redis参数");
        }

        Config config = new Config();
        String ip = protocolProperty.getIp();

        String[] split = StrUtil.splitToArray(ip, ",");
        if (ArrayUtil.isEmpty(split)) {
            throw new IllegalArgumentException("未配置Redis参数");
        }

        int length = split.length;
        //单机模式
        if (length == 1) {
            config.useSingleServer().setAddress("redis://" + protocolProperty.getIp());
        } else {
            for (String s : split) {
                config.useClusterServers().addNodeAddress("redis://" + s);
            }
        }

        this.client = Redisson.create(config);
    }

    @Override
    public Output outputGene(Protocol info) {
        return message -> {
            RTopic topic = client.getTopic(info.getTopic(), new SerializationCodec());
            topic.publish(message);
            return null;
        };
    }

    @Override
    public Input inputGene(Protocol info, Input input) {
        String server = info.getTopic();

        if (listenerMap.containsKey(server)) {
            log.error("该监听已存在");
            return input;
        }

        RTopic topic = client.getTopic(server, new SerializationCodec());

        MessageListener<String> listen = (channel, msg) -> {
            try {
                input.invoke(msg);
            } catch (Exception e) {
                log.error("接收redis消息失败");
            }
        };

        topic.addListener(String.class, listen);
        listenerMap.put(server, listen);
        return input;
    }
}
