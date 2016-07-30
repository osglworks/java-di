package org.osgl.inject;

/**
 * Handle 404
 */
public class NotFoundHandler extends ErrorHandler {
    @Override
    public int getErrorCode() {
        return 404;
    }
}
