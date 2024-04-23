package com.jimmy.friday.agent.other;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MultiMap<K, V> {

    private final ConcurrentHashMap<K, ConcurrentLinkedQueue<V>> map = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        map.compute(key, (k, queue) -> {
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
            }
            queue.add(value);
            return queue;
        });
    }

    public V get(K key) {
        ConcurrentLinkedQueue<V> queue = map.get(key);
        if (queue != null) {
            V value = queue.poll();
            if (value != null && queue.isEmpty()) {
                map.compute(key, (k, q) -> queue.isEmpty() ? null : queue);
            }
            return value;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }
}
