package com.jimmy.friday.center.core.gateway.grpc;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;

import java.util.Map;

public class GrpcErrorMapper {

    private static final Map<String, String> MAPPER = Maps.newHashMap();

    static {
        MAPPER.put("50", "PDF解析成功");
        MAPPER.put("51", "PDF解析失败");
        MAPPER.put("52", "PDF清洗失败");
        MAPPER.put("53", "文件不合规");
        MAPPER.put("54", "文件下载失败");
        MAPPER.put("360", "任务执行失败");
        MAPPER.put("361", "任务执行超时1200秒");
        MAPPER.put("319", "拼凑pdf出错");
        MAPPER.put("320", "拼凑pdf出错");
        MAPPER.put("301", "下载出错");
        MAPPER.put("353", "PDF不完整");
    }

    public static String errorCodeMapper(String code) {
        String s = MAPPER.get(code);
        return StrUtil.isEmpty(s) ? "grpc调用失败" : s;
    }
}
