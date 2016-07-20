package org.osgl.genie;

import org.osgl.genie.annotation.MapKey;
import org.osgl.genie.annotation.TypeOf;

import javax.inject.Inject;
import java.util.Map;

/**
 * Dispatch error to proper handlers
 */
class ErrorDispatcher {
    @Inject
    @TypeOf(ErrorHandler.class)
    @MapKey("errorCode")
    Map<Integer, ErrorHandler> registry;

    String handle(int error) {
        ErrorHandler handler = registry.get(error);
        return null == handler ? "unknown" : handler.toString();
    }
}
