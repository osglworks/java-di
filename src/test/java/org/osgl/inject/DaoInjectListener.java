package org.osgl.inject;

import org.osgl.$;

import java.lang.reflect.Type;
import java.util.List;

public class DaoInjectListener extends InjectListener.Adaptor {
    @Override
    public void injected(Object bean, BeanSpec beanSpec) {
        if (bean instanceof Dao) {
            Dao dao = $.cast(bean);
            List<Type> typeParams = beanSpec.typeParams();
            dao.setModelType((Class)typeParams.get(0));
        }
    }
}
