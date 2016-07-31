package org.osgl.inject;

import javax.inject.Inject;

class DerivedFromBaseWithPostConstructor extends BaseWithPostConstructor {
    static class Holder {
        @Inject
        BaseWithPostConstructor bean;
    }

    static class Module extends org.osgl.inject.Module {
        @Override
        protected void configure() {
            bind(BaseWithPostConstructor.class).to(DerivedFromBaseWithPostConstructor.class);
        }
    }
}
