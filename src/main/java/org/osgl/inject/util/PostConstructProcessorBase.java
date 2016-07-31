package org.osgl.inject.util;

import org.osgl.Osgl;
import org.osgl.inject.PostConstructProcessor;

/**
 * Base class for implementing {@link org.osgl.inject.PostConstructProcessor}
 */
public abstract class PostConstructProcessorBase<T> extends Osgl.Visitor<T> implements PostConstructProcessor<T> {
}
