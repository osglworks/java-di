package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
