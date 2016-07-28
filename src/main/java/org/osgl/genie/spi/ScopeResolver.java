package org.osgl.genie.spi;

import javax.inject.Provider;
import javax.inject.Scope;
import java.lang.annotation.Annotation;

/**
 * Implementation shall provide logic to check if an annotation is a scope annotation
 */
public interface ScopeResolver {
    /**
     * Check if an annotation class is a {@link javax.inject.Scope} annotation
     * @return
     */
    boolean isScope(Class<? extends Annotation> annoClass);

    /**
     * The built-in scope resolver will check if the annotation class has
     * {@link Scope} annotation presented
     */
    public static class BuiltInScopeResolver implements ScopeResolver, Provider<ScopeResolver> {

        public static final BuiltInScopeResolver INSTANCE = new BuiltInScopeResolver();

        @Override
        public boolean isScope(Class<? extends Annotation> annoClass) {
            return annoClass.isAnnotationPresent(Scope.class);
        }

        @Override
        public ScopeResolver get() {
            return this;
        }
    }
}
