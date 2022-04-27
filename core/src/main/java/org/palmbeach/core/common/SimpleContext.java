package org.palmbeach.core.common;

import lombok.NonNull;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ToString
public class SimpleContext implements Context {

    // Variables.

    private final Map<String, Object> map;

    // Constructors.

    public SimpleContext() {
        this.map = new ConcurrentHashMap<>();
    }

    // Methods.

    @Override
    public void map(@NonNull String key, @NonNull Object value) {
        map.put(key, value);
    }

    @Override
    public Object getValue(@NonNull String key) {
        return map.get(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
