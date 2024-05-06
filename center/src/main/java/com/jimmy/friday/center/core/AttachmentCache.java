package com.jimmy.friday.center.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.center.base.Obtain;
import com.jimmy.friday.center.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AttachmentCache {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Long decrement(String key, Long i) {
        return stringRedisTemplate.opsForValue().decrement(key, i);
    }

    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    public Long increment(String key, Long i) {
        return stringRedisTemplate.opsForValue().increment(key, i);
    }

    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    public void expire(String key, Long i, TimeUnit timeUnit) {
        stringRedisTemplate.expire(key, i, timeUnit);
    }

    public Iterable<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    public <T> Map<String, T> attachMap(String key, Class<T> clazz) {
        Objects.requireNonNull(key);

        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (MapUtil.isEmpty(entries)) {
            return Maps.newHashMap();
        }

        Map<String, T> map = Maps.newHashMap();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            map.put(entry.getKey().toString(), JsonUtil.parseObject(entry.getValue().toString(), clazz));
        }

        return map;
    }

    public void attachList(String key, Object attachment) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(attachment);

        stringRedisTemplate.opsForSet().add(key, JsonUtil.toString(attachment));
    }

    public void attachStringList(String key, String attachment) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(attachment);

        stringRedisTemplate.opsForSet().add(key, attachment);
    }

    public <T> List<T> attachmentList(String key, Class<T> clazz) {
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (CollUtil.isEmpty(members)) {
            return Lists.newArrayList();
        }

        List<T> result = Lists.newArrayList();
        for (String member : members) {
            T t = JsonUtil.parseObject(member, clazz);
            if (t != null) {
                result.add(t);
            }
        }

        return result;
    }

    public <T> List<T> attachmentList(String key, Class<T> clazz, Obtain<List<T>> obtain) {
        List<T> ts = this.attachmentList(key, clazz);
        if (CollUtil.isNotEmpty(ts)) {
            return ts;
        }

        ts = obtain.obtain();
        if (CollUtil.isEmpty(ts)) {
            return Lists.newArrayList();
        }

        for (T t : ts) {
            this.attachList(key, t);
        }

        return ts;
    }

    public void attach(String mainKey, String key, Object attachment) {
        Objects.requireNonNull(mainKey);
        Objects.requireNonNull(key);
        Objects.requireNonNull(attachment);

        this.attachString(mainKey, key, JsonUtil.toString(attachment));
    }

    public void attach(String key, Object attachment) {
        this.attachString(key, JsonUtil.toString(attachment));
    }

    public void attachString(String key, String attachment) {
        stringRedisTemplate.opsForValue().set(key, attachment);
    }

    public void attachString(String key, String attachment, Long timeout, TimeUnit timeUnit) {
        if (timeout != null && timeout > 0) {
            stringRedisTemplate.opsForValue().set(key, attachment, timeout, timeUnit);
        } else {
            stringRedisTemplate.opsForValue().set(key, attachment);
        }
    }

    public void attachString(String mainKey, String key, String attachment) {
        stringRedisTemplate.opsForHash().put(mainKey, key, attachment);
    }

    public void mapper(String mainKey, String key, Object value) {
        Objects.requireNonNull(mainKey);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        this.attachString(mainKey, key, value.toString());
    }

    public boolean setIfAbsent(String key, String attachment) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, attachment));
    }

    public boolean setIfAbsent(String key, String attachment, Long time, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, attachment, time, timeUnit));
    }

    public boolean putIfAbsent(String mainKey, String key, String attachment) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForHash().putIfAbsent(mainKey, key, attachment));
    }

    public <T> T attachment(String mainKey, String key, Class<T> clazz) {
        Object o = this.attachment(mainKey, key);
        return o == null ? null : JsonUtil.parseObject(o.toString(), clazz);
    }

    public Object attachment(String mainKey, String key) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(mainKey);
        return stringRedisTemplate.opsForHash().get(mainKey, key);
    }

    public String attachment(String key) {
        Objects.requireNonNull(key);
        return stringRedisTemplate.opsForValue().get(key);
    }

    public <T> T attachment(String mainKey, String key, Class<T> clazz, Obtain<T> obtain) {
        T attachment = this.attachment(mainKey, key, clazz);
        if (attachment != null) {
            return attachment;
        }

        attachment = obtain.obtain();
        if (attachment == null) {
            return null;
        }

        this.attach(mainKey, key, attachment);
        return attachment;
    }

    public <T> T attachment(String key, Class<T> clazz, Obtain<T> obtain) {
        T attachment = this.attachment(key, clazz);
        if (attachment != null) {
            return attachment;
        }

        attachment = obtain.obtain();
        if (attachment == null) {
            return null;
        }

        this.attach(key, attachment);
        return attachment;
    }

    public <T> T attachment(String key, Class<T> clazz) {
        String o = this.attachment(key);
        return StrUtil.isEmpty(o) ? null : JsonUtil.parseObject(o, clazz);
    }

    public void remove(String mainKey, String key) {
        stringRedisTemplate.opsForHash().delete(mainKey, key);
    }

    public void remove(String mainKey) {
        stringRedisTemplate.delete(mainKey);
    }
}
