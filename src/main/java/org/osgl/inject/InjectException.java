package org.osgl.inject;

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
