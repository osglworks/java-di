package org.osgl.genie;

import org.osgl.genie.annotation.Provides;

class ModuleWithFactories {

    @Provides
    public Person male(Person.Man man) {
        return man;
    }

    @Provides
    @Person.Female
    public Person female(Person.Woman woman) {
        return woman;
    }

}
