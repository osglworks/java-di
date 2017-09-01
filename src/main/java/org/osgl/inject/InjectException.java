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

import org.osgl.exception.UnexpectedException;

/**
 * `InjectException` is thrown out when error occurred within
 * dependency injection process
 */
public class InjectException extends UnexpectedException {

    public InjectException(String message, Object... args) {
        super(message, args);
    }

    public InjectException(Throwable cause) {
        super(cause);
    }

    public InjectException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public static InjectException circularDependency(CharSequence dependencyChain) {
        return new InjectException("Circular dependency found: %s", dependencyChain);
    }
}
