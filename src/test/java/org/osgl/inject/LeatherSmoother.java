package org.osgl.inject;

import org.osgl.inject.annotation.Provides;

import javax.inject.Inject;

public interface LeatherSmoother {

    class RedLeatherSmoother implements LeatherSmoother {}

    class BlackLeatherSmoother implements LeatherSmoother {}

    class TanLeatherSmoother implements LeatherSmoother {}

    class Module {
        @Leather(color = Leather.Color.RED)
        @Provides
        public static LeatherSmoother red(RedLeatherSmoother smoother) {
            return smoother;
        }
        @Leather(color = Leather.Color.BLACK)
        @Provides
        public static LeatherSmoother black(BlackLeatherSmoother smoother) {
            return smoother;
        }
        @Leather(color = Leather.Color.TAN)
        @Provides
        public static LeatherSmoother redOne(TanLeatherSmoother smoother) {
            return smoother;
        }
    }

    class DynamicModule {
        @Provides
        public static LeatherSmoother find(BeanSpec spec) {
            Leather leather = spec.getAnnotation(Leather.class);
            if (null != leather) {
                return leather.color().smoother();
            }
            return null;
        }
    }

    class Host {
        LeatherSmoother smoother;

        @Inject
        public Host(@Leather(color = Leather.Color.RED) LeatherSmoother smoother) {
            this.smoother = smoother;
        }
    }
}
