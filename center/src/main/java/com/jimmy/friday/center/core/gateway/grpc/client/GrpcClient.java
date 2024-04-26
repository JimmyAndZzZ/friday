package com.jimmy.friday.center.core.gateway.grpc.client;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.center.proto.GwPyReply;
import com.jimmy.friday.center.proto.GwPyRequest;
import com.jimmy.friday.center.utils.JsonUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class GrpcClient {

    private static final String RESULT_KEY = "result";

    protected final String ip;

    protected final ManagedChannel channel;

    protected final ApplicationContext applicationContext;

    public GrpcClient(String host, int port, ApplicationContext applicationContext) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        this.ip = host + ":" + port;
        this.applicationContext = applicationContext;
    }

    public abstract GwPyReply execute(Service service, Method method, Map<String, String> args);

    public void shutdown() throws InterruptedException {
        channel.shutdownNow();
    }

    public GwPyRequest requestConvert(Map<String, String> args) {
        Map<String, String> result = Maps.newHashMap();
        if (MapUtil.isNotEmpty(args)) {
            Set<String> keySet = args.keySet();
            for (String s : keySet) {
                String value = args.get(s);
                if (StrUtil.isEmpty(value)) {
                    result.put(s, value);
                    continue;
                }

                JsonNode convert = JsonUtil.parse(value);
                result.put(s, convert == null ? null : convert.asText());
            }
        }

        return GwPyRequest.newBuilder().putAllArgs(result).build();
    }

    public Object responseConvert(GwPyReply gwPyReply) {
        Map<String, String> response = gwPyReply.getMessageMap();
        if (MapUtil.isEmpty(response)) {
            return null;
        }

        String s = response.get(RESULT_KEY);
        return StrUtil.isEmpty(s) ? null : this.convert(s);
    }

    /**
     * 值转换
     *
     * @param s
     * @return
     */
    private Object convert(String s) {
        JSONValidator.Type type = JSONValidator.from(s).getType();

        switch (type) {
            case Array:
                return JSON.parseArray(s);
            case Value:
                return s;
            case Object:
                return JSON.parseObject(s);
            default:
                return null;
        }
    }
}
