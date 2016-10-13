package org.osgl.inject;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(RUNTIME)
@javax.inject.Qualifier
public @interface Leather {
    Color color() default Color.TAN;
    enum Color {
        RED () {
            @Override
            public LeatherSmoother smoother() {
                return new LeatherSmoother.RedLeatherSmoother();
            }
        }, BLACK() {
            @Override
            public LeatherSmoother smoother() {
                return new LeatherSmoother.BlackLeatherSmoother();
            }
        }, TAN() {
            @Override
            public LeatherSmoother smoother() {
                return new LeatherSmoother.TanLeatherSmoother();
            }
        };

        public abstract LeatherSmoother smoother();
    }
}