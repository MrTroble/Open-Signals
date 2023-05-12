package com.troblecodings.signals.core;

import java.util.Map.Entry;

public class OSMapEntry<K, V> implements Entry<K, V> {

    private final K key;
    private V value;

    public OSMapEntry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(final V value) {
        this.value = value;
        return value;
    }
}