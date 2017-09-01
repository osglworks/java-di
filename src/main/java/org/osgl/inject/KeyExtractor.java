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

import org.osgl.$;

/**
 * `KeyExtractor` can be used to extract or derive "key" from
 * a value data. The result "key" can be used to index the value
 * in a {@link java.util.Map} data structure.
 * <p>
 * A general contract implementation must obey is
 * different value must generate different key
 *
 * @param <K> generic type of the key
 * @param <V> generic type of the value
 */
public interface KeyExtractor<K, V> {
    /**
     * Get the key of data
     *
     * @param hint optional, a string value provides hint to extract key
     * @param data the value data
     * @return the key of the data
     */
    K keyOf(String hint, V data);

    class PropertyExtractor implements KeyExtractor {
        @Override
        public Object keyOf(String hint, Object data) {
            return $.getProperty(data, hint);
        }
    }
}
