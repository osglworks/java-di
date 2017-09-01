package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
