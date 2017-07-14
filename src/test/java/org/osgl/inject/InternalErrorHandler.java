package org.osgl.inject;

import javax.inject.Singleton;

/**
 * Handle 500
 */
@Singleton
public class InternalErrorHandler extends ErrorHandler {
    @Override
    public int getErrorCode() {
        return 500;
    }
}
