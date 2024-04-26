package com.jimmy.friday.center.core.schedule;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.center.core.StripedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class ScheduleSession {

    private static final String LOCK_NAME = "scheduleLock";

    private final ConcurrentMap<String, Set<String>> session = Maps.newConcurrentMap();

    @Autowired
    private StripedLock stripedLock;

    public void connect(String applicationId, String applicationName) {
        Lock lock = stripedLock.getLocalLock(LOCK_NAME, 16, applicationId);

        lock.lock();
        try {
            Set<String> put = session.put(applicationName, Sets.newHashSet(applicationId));
            if (put != null) {
                put.add(applicationId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void disconnect(String applicationId, String applicationName) {
        Lock lock = stripedLock.getLocalLock(LOCK_NAME, 16, applicationId);

        lock.lock();
        try {
            Set<String> strings = session.get(applicationName);
            if (strings != null) {
                strings.remove(applicationId);
            }
        } finally {
            lock.unlock();
        }
    }

}
