package com.jimmy.friday.center.core.schedule;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.center.core.StripedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ScheduleSession {

    private final ConcurrentMap<String, Set<String>> session = Maps.newConcurrentMap();

    @Autowired
    private StripedLock stripedLock;

    public void connect(String applicationId, String applicationName) {


        Set<String> put = session.put(applicationName, Sets.newHashSet(applicationId));
        if (put != null) {
            put.add(applicationId);
        }
    }

}
