package components.mapper;

import org.citygml4j.model.common.base.ModelClassEnum;

import java.time.ZonedDateTime;

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
    STRING(String.class),
    ZONED_DATE_TIME(ZonedDateTime.class);
    // TODO More simple classes?

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

    /**
     * Determines whether a given object is of a simple type, meaning their contents can be easily displayed as texts.
     *
     * @param object
     * @return {@code true} if the object is of a simple type; {@code false} otherwise.
     */
    public static boolean isPrintable(Object object) {
        Class cl = object.getClass();
        for (PrintableClass p : values()) {
            if (p.modelClass == cl) {
                return true;
            }
        }
        return false;
    }

    public Class<?> getModelClass() {
        return modelClass;
    }
}
