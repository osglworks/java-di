package org.osgl.genie;

/**
 * A test class: define a abstract class for error handlers
 */
public abstract class ErrorHandler {
    /**
     * Returns the error code the implementation is looking for
     * @return the interested error code
     */
    public abstract int getErrorCode();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
