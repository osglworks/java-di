package org.osgl.genie;

import org.osgl.$;

/**
 * `KeyExtractor` can be used to extract or derive "key" from
 * a value data. The result "key" can be used to index the value
 * in a {@link java.util.Map} data structure.
 *
 * A general contract implementation must obey is
 * different value must generate different key
 *
 * @param <K> generic type of the key
 * @param <V> generic type of the value
 */
public interface KeyExtractor<K, V> {
    /**
     * Get the key of data
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
