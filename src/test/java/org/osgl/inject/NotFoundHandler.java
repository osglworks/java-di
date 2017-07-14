package org.osgl.inject;

import javax.inject.Singleton;

/**
 * Handle 404
 */
@Singleton
public class NotFoundHandler extends ErrorHandler {
    @Override
    public int getErrorCode() {
        return 404;
    }
}
