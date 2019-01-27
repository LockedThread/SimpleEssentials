package rip.simpleness.simpleessentials.map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.HashSet;

public final class SimplenessMap<K extends String, V> extends HashMap<K, V> {

    public V putAndReturn(K key, V value) {
        put(key, value);
        return value;
    }

    public ImmutableMap<K, V> toImmutableMap() {
        return ImmutableMap.copyOf(this);
    }

    public ImmutableSet<K> toImmutableSetValue() {
        return ImmutableSet.copyOf(this.keySet());
    }

    public ImmutableSet<V> toImmutableSetKey() {
        return ImmutableSet.copyOf(this.values());
    }

    public HashSet<V> valueSet() {
        return new HashSet<>(super.values());
    }
}
