package org.osgl.genie;

/**
 * Handle 404
 */
public class NotFoundHandler extends ErrorHandler {
    @Override
    public int getErrorCode() {
        return 404;
    }
}
