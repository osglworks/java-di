package org.osgl.inject;

import org.osgl.util.C;

import java.util.Map;
import java.util.UUID;

/**
 * Emulate computational context
 */
class Context {

    private String id;

    private Map<String, Object> data = C.newMap();

    Context() {
        id = UUID.randomUUID().toString();
    }

    public String id() {
        return id;
    }

    <T> T get(String key) {
        return (T) data.get(key);
    }

    <T> void put(String key, T object) {
        data.put(key, object);
    }

    private static final ThreadLocal<Context> current = new ThreadLocal<Context>();

    static Context get() {
        return current.get();
    }
    static void set(Context context) {
        current.set(context);
    }
    static void reset() {
        current.remove();
    }
}
