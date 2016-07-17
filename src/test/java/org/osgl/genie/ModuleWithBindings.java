package org.osgl.genie;

class ModuleWithBindings extends Module {

    @Override
    protected void configure() {
        bind(Person.class).to(Person.Man.class);
        bind(Person.class).withAnnotation(Person.Female.class).to(Person.Woman.class);
    }

}
