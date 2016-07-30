package org.osgl.inject;

import org.osgl.inject.annotation.Provides;

abstract class ModuleWithStaticFactories {

    @Provides
    public static Person male(Person.Man man) {
        return man;
    }

    @Provides
    @Person.Female
    public static Person female(Person.Woman woman) {
        return woman;
    }

}
