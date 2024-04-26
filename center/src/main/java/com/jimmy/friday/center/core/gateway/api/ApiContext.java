package com.jimmy.friday.center.core.gateway.api;

import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.center.base.Hook;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
public class ApiContext extends HashMap<String, Object> {

    private final List<Hook> hooks = Lists.newArrayList();

    public void register(Hook hook) {
        this.hooks.add(hook);
    }

    public String getString(String key) {
        return MapUtil.getStr(this, key);
    }

    public Long getLong(String key) {
        return MapUtil.getLong(this, key);
    }

    public Integer getInt(String key) {
        return MapUtil.getInt(this, key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return MapUtil.get(this, key, clazz);
    }
}
