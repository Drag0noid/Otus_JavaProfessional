package ru.otus.cachehw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyCache<K, V> implements HwCache<K, V> {

    private static final Logger log = LoggerFactory.getLogger(MyCache.class);

    private final Map<K, V> cache = new WeakHashMap<>();
    private final List<HwListener<K, V>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        notificationListeners(key, value, "put");
    }

    @Override
    public void remove(K key) {
        var value = cache.remove(key);
        notificationListeners(key, value, "remove");
    }

    @Override
    public V get(K key) {
        var value = cache.get(key);
        notificationListeners(key, value, "get");
        return value;
    }

    @Override
    public void addListener(HwListener<K, V> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(HwListener<K, V> listener) {
        listeners.remove(listener);
    }

    @Override
    public int size() {
        int size = 0;
        for (Map.Entry<K, V> entry : cache.entrySet()) {
            if (entry.getValue() != null) {
                size++;
            }
        }
        return size;
    }

    private void notificationListeners(K key, V value, String action) {
        for (HwListener<K, V> listener : listeners) {
            try {
                listener.notify(key, value, action);
            } catch (Exception e) {
                log.error(
                        "Exception occurred while notifying listener: key={}, value={}, action={}, error={}",
                        key,
                        value,
                        action,
                        e.getMessage(),
                        e);
            }
        }
    }
}