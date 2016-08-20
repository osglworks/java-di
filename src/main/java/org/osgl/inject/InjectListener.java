package org.osgl.inject;

public interface InjectListener {
    void providerRegistered(Class targetType);
    void injected(Object bean, BeanSpec beanSpec);

    class Adaptor implements InjectListener {
        @Override
        public void providerRegistered(Class targetType) {
        }

        @Override
        public void injected(Object bean, BeanSpec beanSpec) {
        }
    }
}
