package org.osgl.inject;

import org.osgl.$;

public class Dao<T> {

    private Class<T> modelType;

    Class<T> modelType() {
        return this.modelType;
    }
    void setModelType(Class<T> modelType) {
        this.modelType = $.notNull(modelType);
    }
}
