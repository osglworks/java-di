package org.osgl.inject.loader;

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
import org.osgl.inject.BeanSpec;
import org.osgl.inject.InjectException;
import org.osgl.inject.ValueLoader;
import org.osgl.util.S;
import org.osgl.util.StringValueResolver;

/**
 * Load value from configuration source
 */
public abstract class ConfigurationValueLoader<T> extends ValueLoader.Base<T> {

    @Override
    public T get() {
        String confKey = value();
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
