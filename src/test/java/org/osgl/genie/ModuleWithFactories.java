package org.osgl.genie;

import org.osgl.genie.annotation.Provides;

class ModuleWithFactories {

    @Provides
    public Person male() {
        return new Person.Man();
    }

    @Provides
    @Person.Female
    public Person female() {
        return new Person.Woman();
    }

}
