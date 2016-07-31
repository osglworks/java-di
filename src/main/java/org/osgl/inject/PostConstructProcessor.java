package org.osgl.inject;

import org.osgl.$;

/**
 * Define the logic that needs to be invoked on the bean before return back
 */
public interface PostConstructProcessor<T> extends $.Func1<T, Void> {
}
