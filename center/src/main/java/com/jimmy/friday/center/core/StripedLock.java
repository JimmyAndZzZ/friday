package com.jimmy.friday.center.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.base.Obtain;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
public class StripedLock {

    private final Set<String> lockKeys = Sets.newHashSet();

    private final ConcurrentMap<String, Striped<Lock>> stripedLock = Maps.newConcurrentMap();

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttachmentCache attachmentCache;

    public void tryLock(String key, Long time, TimeUnit timeUnit, Runnable runnable) {
        if (this.tryLock(key, time, timeUnit)) {
            try {
                lockKeys.add(key);
                runnable.run();
            } finally {
                this.releaseLock(key);
                lockKeys.remove(key);
            }
        }
    }

    public Lock getLocalLock(String name, int stripes, Object key) {
        Striped<Lock> lockStriped = stripedLock.get(name);
        if (lockStriped != null) {
            return lockStriped.get(key);
        }

        Striped<Lock> lock = Striped.lock(stripes);
        Striped<Lock> ifPresent = stripedLock.putIfAbsent(name, lock);
        return ifPresent != null ? ifPresent.get(key) : lock.get(key);
    }

    public RReadWriteLock getDistributedReadWriteLock(String key) {
        return redissonClient.getReadWriteLock(key);
    }

    public Lock getDistributedLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * 尝试加锁
     *
     * @param key
     * @param time
     * @param timeUnit
     * @return
     */
    private boolean tryLock(String key, Long time, TimeUnit timeUnit) {
        return attachmentCache.setIfAbsent(key, YesOrNoEnum.YES.getCode(), time, timeUnit);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    private void releaseLock(String key) {
        attachmentCache.remove(key);
    }
}
