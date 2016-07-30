package org.osgl.inject;

import org.osgl.inject.annotation.MapKey;
import org.osgl.inject.annotation.TypeOf;

import javax.inject.Inject;
import java.util.Map;

/**
 * Dispatch error to proper handlers
 */
class ErrorDispatcher {
    @Inject
    @TypeOf
    @MapKey("errorCode")
    Map<Integer, ErrorHandler> registry;

    String handle(int error) {
        ErrorHandler handler = registry.get(error);
        return null == handler ? "unknown" : handler.toString();
    }
}
