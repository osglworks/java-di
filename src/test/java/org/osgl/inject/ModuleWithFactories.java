package org.osgl.inject;

import org.osgl.inject.annotation.Provides;

class ModuleWithFactories {

    @Provides
    public Person male(Person.Man man) {
        return man;
    }

    @Provides
    @Person.Female
    public static Person female(Person.Woman woman) {
        return woman;
    }

}
