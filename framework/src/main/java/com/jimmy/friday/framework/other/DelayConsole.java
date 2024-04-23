package com.jimmy.friday.framework.other;


import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DelayConsole {

    private final Map<String, Delay> delayMap = Maps.newConcurrentMap();

    public void add(String key, Runnable runnable, Integer timeout, TimeUnit timeUnit) {
        Delay delay = new Delay(runnable);

        Delay put = delayMap.put(key, delay);
        if (put != null) {
            put.interrupted();
        }

        delay.await(timeout, timeUnit, key);
    }

    private class Delay {

        private final Runnable runnable;

        private final CountDownLatch countDownLatch = new CountDownLatch(1);

        public Delay(Runnable runnable) {
            this.runnable = runnable;
        }

        public void await(Integer timeout, TimeUnit timeUnit, String id) {
            try {
                countDownLatch.await(timeout, timeUnit);
                //等待超时
                if (countDownLatch.getCount() != 0L) {
                    try {
                        runnable.run();
                    } finally {
                        delayMap.remove(id);
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }

        public void interrupted() {
            countDownLatch.countDown();
        }
    }
}
