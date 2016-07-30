package org.osgl.inject.loader;

import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.InjectException;
import org.osgl.inject.ValueLoader;
import org.osgl.util.S;
import org.osgl.util.StringValueResolver;

import java.util.Map;

/**
 * Load value from configuration source
 */
public abstract class ConfigurationValueLoader<T> implements ValueLoader<T> {
    @Override
    public T load(Map<String, Object> options, BeanSpec spec) {
        String confKey = (String) options.get("value");
        if (S.isBlank(confKey)) {
            throw new InjectException(("Missing configuration key"));
        }
        Object conf = conf(confKey);
        if (null == conf) {
            return null;
        }
        return cast(conf, spec);
    }

    /**
     * Return whatever configured by `key`
     * @param key the configuration key
     * @return the configured value
     */
    protected abstract Object conf(String key);

    private T cast(Object val, BeanSpec spec) {
        Class<?> type = spec.rawType();
        if (type.isInstance(val)) {
            return (T) val;
        }
        if ($.isSimpleType(type)) {
            StringValueResolver svr = StringValueResolver.predefined(type);
            if (null != svr) {
                return (T) svr.resolve(S.string(val));
            }
        }
        throw new InjectException("Cannot cast value type[%s] to required type[%]", val.getClass(), type);
    }
}
