package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.inject.Provider;
import java.lang.annotation.Annotation;

/**
 * A generic Injector interface defines the contract
 * a dependency injector like {@link Genie} should provide
 */
public interface Injector {
    /**
     * Returns a bean of given type
     *
     * @param type the class of the bean
     * @param <T>  generic type of the bean
     * @return the bean
     */
    <T> T get(Class<T> type);

    /**
     * Returns a provider of given type
     *
     * @param type the class of the bean that provider provides
     * @param <T>  the generic type of the bean
     * @return the provider that provides the bean
     */
    <T> Provider<T> getProvider(Class<T> type);

    /**
     * Check if a supplied annotation class is a {@link javax.inject.Qualifier}
     *
     * @param annoClass
     * @return `true` if the annotation type is a qualifier or `false` otherwise
     */
    boolean isQualifier(Class<? extends Annotation> annoClass);

    /**
     * Check if a supplied annotation class specifies a {@link PostConstructProcessor}
     *
     * @param annoClass the annotation type
     * @return `true` if the annotation type specifies post construct processor
     */
    boolean isPostConstructProcessor(Class<? extends Annotation> annoClass);

    /**
     * Check if a supplied annotation class is a {@link javax.inject.Scope} annotation
     *
     * @param annoClass the annotation type
     * @return `true` if the annotation type indicate a scope
     */
    boolean isScope(Class<? extends Annotation> annoClass);

    /**
     * Check if a supplied annotation class is a {@link org.osgl.inject.annotation.StopInheritedScope} annotation or alias of that annotation
     *
     * @param annoClass the annotation type
     * @return `true` if the annotation type is inherited scope stopper
     */
    boolean isInheritedScopeStopper(Class<? extends Annotation> annoClass);

    /**
     * Returns the scope annotation type from an alias annotation type
     * @param alias the alias of the scope annotation
     * @return the scope annotation type if found
     */
    Class<? extends Annotation> scopeByAlias(Class<? extends Annotation> alias);
}
