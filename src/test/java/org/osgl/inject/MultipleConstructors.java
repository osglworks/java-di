package org.osgl.inject;

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
