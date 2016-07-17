package org.osgl.genie;

import org.osgl.genie.annotation.Filter;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Filter(EvenNumberFilter.class)
public @interface EvenNumber {
}
