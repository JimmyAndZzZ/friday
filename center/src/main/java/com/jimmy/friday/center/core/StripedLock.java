package com.jimmy.friday.center.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.base.Close;
import com.jimmy.friday.center.base.Obtain;
import com.jimmy.friday.center.utils.RedisConstants;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
public class StripedLock implements Close {

    private final Set<String> lockKeys = Sets.newHashSet();

    private final Map<String, RLock> readLocks = Maps.newConcurrentMap();

    private final Map<String, RLock> writeLocks = Maps.newConcurrentMap();

    private final ConcurrentMap<String, Striped<Lock>> stripedLock = Maps.newConcurrentMap();

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public void close() {
        if (CollUtil.isNotEmpty(lockKeys)) {
            for (String lockKey : lockKeys) {
                attachmentCache.remove(lockKey);
            }
        }

        if (MapUtil.isNotEmpty(readLocks)) {
            for (RLock value : readLocks.values()) {
                value.unlock();
            }
        }

        if (MapUtil.isNotEmpty(writeLocks)) {
            for (RLock value : writeLocks.values()) {
                value.unlock();
            }
        }
    }

    public <T> T tryLock(String key, Long time, TimeUnit timeUnit, Obtain<T> obtain, T tryFailResult) {
        if (this.tryLock(key, time, timeUnit)) {
            try {
                lockKeys.add(key);
                return obtain.obtain();
            } finally {
                this.releaseLock(key);
                lockKeys.remove(key);
            }
        } else {
            return tryFailResult;
        }
    }

    public void tryLock(String key, Long time, TimeUnit timeUnit, Runnable runnable) {
        if(RedisConstants.Schedule.SCHEDULE_NO_TIMEOUT_JOB_SCAN_LOCK.equals(key)){
            System.out.println(123);
        }

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

    public void readWriteLockRead(String key, Long time, TimeUnit timeUnit, Runnable runnable) {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(key);
        RLock rLock = readWriteLock.readLock();

        rLock.lock(time, timeUnit);
        try {
            readLocks.put(key, rLock);
            runnable.run();
        } finally {
            rLock.unlock();
            readLocks.remove(key);
        }
    }

    public void readWriteLockWrite(String key, Long time, TimeUnit timeUnit, Runnable runnable) {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(key);
        RLock rLock = readWriteLock.writeLock();

        rLock.lock(time, timeUnit);
        try {
            writeLocks.put(key, rLock);
            runnable.run();
        } finally {
            rLock.unlock();
            writeLocks.remove(key);
        }
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
