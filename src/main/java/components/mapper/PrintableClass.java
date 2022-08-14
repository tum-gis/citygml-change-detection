package components.mapper;

import org.citygml4j.model.common.base.ModelClassEnum;

public enum PrintableClass implements ModelClassEnum {
    UNDEFINED(null),
    BOOLEAN(Boolean.class),
    CHARACTER(Character.class),
    BYTE(Byte.class),
    SHORT(Short.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class);

    private final Class<?> modelClass;

    PrintableClass(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public static PrintableClass fromModelClass(Class<?> modelClass) {
        for (PrintableClass c : PrintableClass.values())
            if (c.modelClass == modelClass)
                return c;

        return UNDEFINED;
    }

    public static PrintableClass fromInt(int i) {
        for (PrintableClass c : PrintableClass.values()) {
            if (c.ordinal() == i)
                return c;
        }

        return UNDEFINED;
    }

    public Class<?> getModelClass() {
        return modelClass;
    }
}
