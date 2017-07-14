package org.osgl.inject;

import org.osgl.inject.annotation.MapKey;
import org.osgl.inject.annotation.TypeOf;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Dispatch error to proper handlers
 */
class ErrorDispatcher {

    @TypeOf
    List<ErrorHandler> handlerList;

    @TypeOf
    @MapKey("errorCode")
    Map<Integer, ErrorHandler> registry;

    @Inject
    @MapKey("errorCode")
    Map<Integer, ErrorHandler> registry2;

    String handle(int error) {
        ErrorHandler handler = registry.get(error);
        return null == handler ? "unknown" : handler.toString();
    }

    String handle2(int error) {
        ErrorHandler handler = registry2.get(error);
        return null == handler ? "unknown" : handler.toString();
    }
}
