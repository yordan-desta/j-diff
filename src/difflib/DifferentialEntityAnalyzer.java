package difflib;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import sun.rmi.runtime.Log;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Yordanos Desta, on 5/2/17.
 */

@SuppressWarnings("unchecked")
public class DifferentialEntityAnalyzer<T extends IDifferentiable> {

    private final HashMap<Field, Object> differenceValues = new HashMap<>();

    private DifferentiableLevel differentiableLevel = DifferentiableLevel.SHALLOW_UPDATE;

    private T oldEntity, newEntity, referencer = null;

    private boolean isReferenced = false;

    private static final Map<String, HashSet<Field>> clazzFieldsCache = new LinkedHashMap<>();

    private static final int DEPTH_COUNT_MAX = 2;

    private boolean isRun;

    private int depthCount = 0;

    public DifferentialEntityAnalyzer(@NotNull T oldEntity, @NotNull T newEntity, DifferentiableLevel differentiableLevel) {

        this(oldEntity, newEntity);

        setDifferentiableLevel(differentiableLevel);
    }

    private DifferentialEntityAnalyzer(@NotNull T oldEntity, @NotNull T newEntity, boolean isReferenced, @Nullable T referencer, int depthCount) {

        this(oldEntity, newEntity);

        this.isReferenced = isReferenced;

        this.referencer = referencer;

        this.depthCount = depthCount;

    }

    public DifferentialEntityAnalyzer(@NotNull T oldEntity, @NotNull T newEntity) {

        isRun = false;

        this.oldEntity = oldEntity;

        this.newEntity = newEntity;
    }

    public HashMap<Field, Object> runDifferential() {

        isRun = true;

        if (oldEntity == null || newEntity == null) {

            final String errMsg = "entity values should be different from null";

            throw new DifferentialException(errMsg, DifferentialException.NULL_ENTITY_EXCEPTION, new Throwable(errMsg));


        } else if (!oldEntity.getClazzName().equals(newEntity.getClazzName())) {

            final String errMsg = "entity types are different. They should be of the same class type";

            throw new DifferentialException(errMsg, DifferentialException.TYPE_MISMATCH_EXCEPTION, new Throwable(errMsg));


        } else if (oldEntity.getClass().getAnnotation(Differentiable.class) != null && oldEntity.getClass().getAnnotation(Differentiable.class).ignoreDiff()) {

            final String errMsg = oldEntity.getClazzName() + " is not differentiable class";

            throw new DifferentialException(errMsg, DifferentialException.NON_DIFFERENTIABLE_CLASS_EXCEPTION, new Throwable(errMsg));

        } else if (oldEntity.equals(newEntity)) {

            final HashSet<Field> fields = (HashSet<Field>) getFields(oldEntity.getClass()).clone();

            for (Field field : fields) {

                try {

                    field.setAccessible(true);

                    final Class<?> fieldType = field.getType();

                    if (DifferentiableEntity.class.isAssignableFrom(fieldType)) {

                        if (differentiableLevel == DifferentiableLevel.SHALLOW_IGNORE) continue;

                        T _oldEntity = (T) field.get(oldEntity);

                        T _newEntity = (T) field.get(newEntity);

                        switch (differentiableLevel) {

                            case SHALLOW_UPDATE:

                                this.differenceValues.put(field, _newEntity);

                                break;

                            case DEEP:

                                if (isReferenced) {

                                    if (null != referencer && referencer.equals(_oldEntity))
                                        continue;
                                }

                                final DifferentialEntityAnalyzer<T> differentialEntityAnalyzer = new DifferentialEntityAnalyzer(_oldEntity, _newEntity, true, oldEntity, depthCount + 1);

                                if(depthCount >= DEPTH_COUNT_MAX)

                                    differentialEntityAnalyzer.setDifferentiableLevel(DifferentiableLevel.SHALLOW_UPDATE);

                                else differentialEntityAnalyzer.setDifferentiableLevel(DifferentiableLevel.DEEP);

                                final HashMap<Field, Object> differenceValues = differentialEntityAnalyzer.runDifferential();

                                if (differentialEntityAnalyzer.hasDifference())
                                    this.differenceValues.put(field, differenceValues);

                                continue;

                        }


                    } else if (List.class.isAssignableFrom(fieldType)) {

                        final Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                        if (DifferentiableEntity.class.isAssignableFrom(listType)) {

                            final ArrayList<DifferentiableEntity> oldLists = new ArrayList<>();

                            final ArrayList<DifferentiableEntity> newLists = new ArrayList<>();

                            for (int i = 0; i < ((ArrayList<? extends DifferentiableEntity>) field.get(oldEntity)).size(); i++) {

                                final DifferentiableEntity diffEntity = ((ArrayList<? extends DifferentiableEntity>) field.get(oldEntity)).get(i);

                                oldLists.add(diffEntity);
                            }

                            for (int j = 0; j < ((ArrayList<? extends IDifferentiable>) field.get(newEntity)).size(); j++) {

                                final DifferentiableEntity diffEntity = ((ArrayList<? extends DifferentiableEntity>) field.get(newEntity)).get(j);

                                newLists.add(diffEntity);
                            }

                            if (oldLists.size() <= newLists.size()) {
                                /***
                                 *
                                 */

                            }

                        }

                        continue;
                    }

                    final Object oldValue = field.get(oldEntity);

                    final Object newValue = field.get(newEntity);

                    if (oldValue == null) {

                        if (newValue != null)
                            this.differenceValues.put(field, newValue);

                    } else {

                        if (!oldValue.equals(newValue))
                            this.differenceValues.put(field, newValue);
                    }

                } catch (Throwable throwable) {

                    throw new DifferentialException("unable to assign type value for " + field, DifferentialException.RUNTIME_ERROR, throwable);
                }
            }

            return this.differenceValues;

        } else
            throw new DifferentialException("unable to compare " + oldEntity + " and " + newEntity, DifferentialException.RUNTIME_ERROR, null);
    }

    public boolean hasDifference() {

        if (!isRun)
            runDifferential();

        return !differenceValues.isEmpty();
    }

    public HashMap<Field, Object> getDifferenceValues() {

        if (!isRun)
            return runDifferential();

        return differenceValues;
    }

    private HashSet<Field> getFields(Class<?> clazz) {

        HashSet<Field> fields = null;

        synchronized (clazzFieldsCache) {

            fields = clazzFieldsCache.get(clazz.getSimpleName());
        }

        if (fields == null) {

            final String key = clazz.getSimpleName();

            fields = new HashSet<>();

            while (clazz.getSuperclass() != null) {

                for (Field field : clazz.getDeclaredFields()) {

                    Differentiable differentiable;

                    final Class<?> _clazz = field.getType();

                    if (DifferentiableEntity.class.isAssignableFrom(_clazz)) {

                        differentiable = _clazz.getAnnotation(Differentiable.class);

                        if (differentiable != null && differentiable.ignoreDiff())
                            continue;
                    }

                    differentiable = field.getAnnotation(Differentiable.class);

                    if (differentiable == null || !differentiable.ignoreDiff())
                        fields.add(field);
                }

                clazz = clazz.getSuperclass();
            }

            synchronized (clazzFieldsCache) {

                clazzFieldsCache.put(key, fields);
            }

            return fields;

        } else return fields;
    }

    public DifferentiableLevel getDifferentiableLevel() {
        return differentiableLevel;
    }

    public void setDifferentiableLevel(DifferentiableLevel differentiableLevel) {
        this.differentiableLevel = differentiableLevel;
    }
}
