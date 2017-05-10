package difflib;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Author: Yordanos Desta, on 5/2/17.
 * <p>
 * This class performs a differential operation on IDifferentiable POJOs
 */

@SuppressWarnings("unchecked")
public class DifferentialEntityAnalyzer<T extends IDifferentiable> implements IDifferentialEntityAnalyzer {

    private final HashMap<Field, Object> differenceValues = new HashMap<>();

    private DifferentiableLevel differentiableLevel = DifferentiableLevel.SHALLOW_UPDATE;

    private T oldEntity, newEntity, referencer = null;

    private boolean isReferenced = false;

    private static final Map<String, HashSet<Field>> clazzFieldsCache = new LinkedHashMap<>();

    private static final int DEPTH_COUNT_MAX = 2;

    private boolean isRun = false;

    private int depthCount = 0;

    private static ObjectMapper objectMapper;

    private static final Object mapperSyncLock = new Object();

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

                                if (depthCount >= DEPTH_COUNT_MAX)

                                    differentialEntityAnalyzer.setDifferentiableLevel(DifferentiableLevel.SHALLOW_UPDATE);

                                else differentialEntityAnalyzer.setDifferentiableLevel(DifferentiableLevel.DEEP);

                                final HashMap<Field, Object> differenceValues = differentialEntityAnalyzer.runDifferential();

                                if (differentialEntityAnalyzer.hasDifference())
                                    this.differenceValues.put(field, differenceValues);

                                continue;

                        }


                    } else if (List.class.isAssignableFrom(fieldType)) {

                        /*
                        Ignore checking diff for list items, and take the new list as a diff
                         */

                        final Object newListValues = field.get(newEntity);

                        this.differenceValues.put(field, newListValues);

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
            throwNotRunException();

        return !differenceValues.isEmpty();
    }

    public HashMap<Field, Object> getDifferenceValues() {

        if (!isRun)
            throwNotRunException();

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

    public String getPrettyJson() {

        if (!isRun)
            throwNotRunException();

        final ObjectWriter writer = getObjectMapper().writer().withDefaultPrettyPrinter();

        try {

            return writer.writeValueAsString(differenceValues);

        } catch (JsonProcessingException e) {

            throw new DifferentialException(e.getLocalizedMessage(), DifferentialException.RUNTIME_ERROR, e);
        }

    }

    private void throwNotRunException() {
        throw new DifferentialException("differential has not been run. make sure you have called runDifferential() first.", DifferentialException.RUNTIME_ERROR, null);
    }

    private class FieldSerializer extends JsonSerializer<HashMap<Field, Object>> {

        @Override
        public void serialize(HashMap<Field, Object> fieldObjectHashMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

            jsonGenerator.writeStartObject();

            for (Field field : fieldObjectHashMap.keySet()) {

                if (fieldObjectHashMap.getClass().isAssignableFrom(fieldObjectHashMap.get(field).getClass())) {

                    final ObjectWriter writer = getObjectMapper().writer().withDefaultPrettyPrinter();

                    jsonGenerator.writeStringField(field.getName(), writer.writeValueAsString(fieldObjectHashMap.get(field)));

                    continue;
                }

                jsonGenerator.writeStringField(field.getName(), String.valueOf(fieldObjectHashMap.get(field)));
            }
        }
    }

    private ObjectMapper getObjectMapper() {

        synchronized (mapperSyncLock) {

            if (objectMapper == null) {

                objectMapper = new ObjectMapper();

                final SimpleModule simpleModule = new SimpleModule();

                simpleModule.addSerializer((Class<? extends HashMap<Field,Object>>) differenceValues.getClass(), new FieldSerializer());

                objectMapper.registerModule(simpleModule);

                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            }

            return objectMapper;
        }
    }
}
