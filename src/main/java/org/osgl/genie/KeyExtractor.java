package org.osgl.genie;

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
    K keyOf(V data);
}
