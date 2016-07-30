package org.osgl.inject;

/**
 * Handle 500
 */
public class InternalErrorHandler extends ErrorHandler {
    @Override
    public int getErrorCode() {
        return 500;
    }
}
