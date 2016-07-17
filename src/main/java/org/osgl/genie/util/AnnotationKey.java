package org.osgl.genie.util;

import java.lang.annotation.Annotation;

public class AnnotationKey {

    private Annotation annotation;
    private boolean isProxy = false;

    public AnnotationKey(Annotation annotation) {
        this.annotation = annotation;
        this.isProxy = annotation == null;
    }



}
