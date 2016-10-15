package org.osgl.inject;

import org.osgl.inject.annotation.Provides;

import javax.inject.Provider;

public class ModuleWithConstructorBinding extends Module {

    @Override
    protected void configure() {
        bind(Person.class).toConstructor(Person.Flexible.class, Provider.class);
    }

    @Provides
    public Person.Gender gender() {
        return Person.Gender.F;
    }
}
