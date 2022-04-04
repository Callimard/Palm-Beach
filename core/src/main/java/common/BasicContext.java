package common;

import java.util.HashMap;
import java.util.Map;

public class BasicContext implements Context {

    // Variables.

    private final Map<String, Object> map;

    // Constructors.

    public BasicContext() {
        this.map = new HashMap<>();
    }

    // Methods.

    @Override
    public void map(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public Object getValue(String key) {
        return map.get(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
