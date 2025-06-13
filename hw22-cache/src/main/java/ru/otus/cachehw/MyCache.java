package ru.otus.cachehw;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyCache<K, V> implements HwCache<K, V> {

    private final Map<K, V> cache = new HashMap<>();
    private final List<HwListener<K, V>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        notifyListeners(key, value, "put");
    }

    @Override
    public void remove(K key) {
        V removedValue = cache.remove(key);
        if (removedValue != null) {
            notifyListeners(key, removedValue, "remove");
        }
    }

    @Override
    public V get(K key) {
        V value = cache.get(key);
        if (value != null) {
            notifyListeners(key, value, "get");
        }
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
        return cache.size();
    }

    private void notifyListeners(K key, V value, String action) {
        for (HwListener<K, V> listener : listeners) {
            try {
                listener.notify(key, value, action);
            } catch (Exception e) {
                System.err.println("Listener threw exception: " + e.getMessage());
            }
        }
    }
}
