package com.jimmy.friday.protocol.config;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProtocolProperty {

    private String ip;

    private String username;

    private String password;

    private Integer port;

    @Override
    public String toString() {
        return new StringBuilder(StrUtil.nullToDefault(ip, "")).append(":")
                .append(StrUtil.nullToDefault(username, "")).append(":")
                .append(StrUtil.nullToDefault(password, "")).append(":")
                .append(Convert.toStr(port, "")).toString();
    }
}
