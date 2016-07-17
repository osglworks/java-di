package org.osgl.genie;


import javax.inject.Inject;
import javax.inject.Named;

class TomAndJen {

    @Inject
    @Named("male")
    Person tom;

    @Inject
    @Named("female")
    Person jen;

}
