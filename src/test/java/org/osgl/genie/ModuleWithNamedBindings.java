package org.osgl.genie;

import org.osgl.genie.annotation.Provides;

import javax.inject.Named;

class ModuleWithNamedBindings extends Module {

    @Override
    protected void configure() {
    }

    @Provides
    @Named("male")
    public Person male(Person.Man man) {
        return man;
    }


    @Provides
    @Named("female")
    public Person female(Person.Woman woman) {
        return woman;
    }

}
