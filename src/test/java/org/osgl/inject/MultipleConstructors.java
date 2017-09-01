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

import javax.inject.Inject;

/**
 * Test inject class with multiple constructors of which only one has Inject annotation
 */
public class MultipleConstructors {

    private String id;
    private Order order;

    public MultipleConstructors() {

    }

    public MultipleConstructors(String id) {
        this.id = id;
    }

    @Inject
    public MultipleConstructors(Order order) {
        this.order = order;
    }

    public boolean hasId() {
        return null != id;
    }

    public boolean hasOrder() {
        return null != order;
    }

}
