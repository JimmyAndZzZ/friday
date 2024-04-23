package com.jimmy.friday.center.core;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;
import com.jimmy.friday.center.base.Close;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Component
public class StripedLock implements Close {

    private final Set<String> lockKeys = Sets.newConcurrentHashSet();

    private final ConcurrentMap<String, Striped<Lock>> stripedLock = Maps.newConcurrentMap();

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttachmentCache attachmentCache;

    public Lock getLocalLock(String name, int stripes, Object key) {
        Striped<Lock> lockStriped = stripedLock.get(name);
        if (lockStriped != null) {
            return lockStriped.get(key);
        }

        Striped<Lock> lock = Striped.lock(stripes);
        Striped<Lock> ifPresent = stripedLock.putIfAbsent(name, lock);
        return ifPresent != null ? ifPresent.get(key) : lock.get(key);
    }

    public ReadWriteLock getDistributedReadWriteLock(String key) {
        this.lockKeys.add(key);
        return redissonClient.getReadWriteLock(key);
    }

    public Lock getDistributedLock(String key) {
        this.lockKeys.add(key);
        return redissonClient.getLock(key);
    }

    @Override
    public void close() {
        if (CollUtil.isNotEmpty(lockKeys)) {
            for (String lockKey : lockKeys) {
                attachmentCache.remove(lockKey);
            }
        }
    }
}
