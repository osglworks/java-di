package org.osgl.inject.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Note this annotation class is borrowed from cdi-api
 *
 * <p>
 * Excludes a member of an annotation type (such as a {@linkplain javax.inject.Qualifier qualifier type} or
 * {@linkplain javax.interceptor interceptor binding type}) from consideration when the container compares two annotation
 * instances.
 * </p>
 *
 * <pre>
 * &#064;Qualifier
 * &#064;Retention(RUNTIME)
 * &#064;Target({ METHOD, FIELD, PARAMETER, TYPE })
 * public @interface PayBy {
 *     PaymentMethod value();
 *
 *     &#064;Nonbinding
 *     String comment();
 * }
 * </pre>
 *
 * @author Gavin King
 *
 * @see javax.inject.Qualifier &#064;Qualifier
 * @see javax.interceptor.InterceptorBinding &#064;InterceptorBinding
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Nonbinding {
}
