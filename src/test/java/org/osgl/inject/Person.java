package org.osgl.inject;

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
