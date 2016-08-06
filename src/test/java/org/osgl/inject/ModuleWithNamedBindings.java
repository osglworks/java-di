package org.osgl.inject;

import org.osgl.inject.annotation.Provides;

import javax.inject.Named;

class ModuleWithNamedBindings extends Module {

    @Override
    protected void configure() {
        bind(Person.class).named("male").to(Person.Man.class);
    }

    @Provides
    @Named("female")
    public Person female(Person.Woman woman) {
        return woman;
    }

}
