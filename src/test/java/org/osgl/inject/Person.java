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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.osgl.inject.Person.Gender.F;
import static org.osgl.inject.Person.Gender.M;

public interface Person {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface Female {
    }

    enum Gender {
        M, F;
        public boolean isFemale() {
            return this == F;
        }
    }

    Gender gender();

    class Man implements Person {
        @Override
        public Gender gender() {
            return M;
        }
    }

    class Woman implements Person {
        @Override
        public Gender gender() {
            return F;
        }
    }

    class Flexible implements Person {

        private Gender gender;

        public Flexible(Provider<Gender> gender) {
            this.gender = gender.get();
        }

        @Override
        public Gender gender() {
            return gender;
        }
    }

    class Family {

        @Inject
        Person dad;

        @Inject
        @Female
        Person mom;

        @Female
        Person daughter;

        Person son;
    }

}
