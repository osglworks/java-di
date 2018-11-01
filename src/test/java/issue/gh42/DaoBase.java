package issue.gh42;

import org.osgl.util.Generics;

import java.lang.reflect.Type;
import java.util.List;

public class DaoBase<ID_TYPE, MODEL_TYPE> implements Dao<ID_TYPE, MODEL_TYPE> {

    public Type modelType;
    public Class<MODEL_TYPE> modelClass;
    public Type idType;
    public Class<ID_TYPE> idClass;

    public DaoBase() {
        exploreTypes();
    }

    private void exploreTypes() {
        List<Type> types = Generics.typeParamImplementations(getClass(), DaoBase.class);
        int sz = types.size();
        if (sz < 1) {
            return;
        }
        if (sz > 1) {
            modelType = types.get(1);
            modelClass = Generics.classOf(modelType);
        }
        idType = types.get(0);
        idClass = Generics.classOf(idType);
    }

}
