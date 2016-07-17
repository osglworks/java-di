package org.osgl.genie;

import org.osgl.genie.annotation.Provides;

class ModuleWithStaticFactories {

    @Provides
    public static Person male() {
        return new Person.Man();
    }

    @Provides
    @Person.Female
    public static Person female() {
        return new Person.Woman();
    }

}
