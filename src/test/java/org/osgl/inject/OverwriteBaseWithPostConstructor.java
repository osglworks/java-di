package org.osgl.inject;

import javax.inject.Inject;

class OverwriteBaseWithPostConstructor extends BaseWithPostConstructor {

    @Override
    protected void init() {
        // Do nothing here
    }

    static class Holder {
        @Inject
        BaseWithPostConstructor bean;
    }

    static class Module extends org.osgl.inject.Module {
        @Override
        protected void configure() {
            bind(BaseWithPostConstructor.class).to(OverwriteBaseWithPostConstructor.class);
        }
    }
}
