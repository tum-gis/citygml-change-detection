package components.mapper;

import org.citygml4j.model.citygml.generics.*;
import org.citygml4j.model.common.base.ModelClassEnum;

public enum GenericAttributeClass implements ModelClassEnum {
    UNDEFINED(null),
    DATE_ATTRIBUTE(DateAttribute.class),
    DOUBLE_ATTRIBUTE(DoubleAttribute.class),
    INT_ATTRIBUTE(IntAttribute.class),
    MEASURE_ATTRIBUTE(MeasureAttribute.class),
    STRING_ATTRIBUTE(StringAttribute.class),
    URI_ATTRIBUTE(UriAttribute.class);

    private final Class<?> modelClass;

    GenericAttributeClass(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public static GenericAttributeClass fromModelClass(Class<?> modelClass) {
        for (GenericAttributeClass c : GenericAttributeClass.values())
            if (c.modelClass == modelClass)
                return c;

        return UNDEFINED;
    }

    public static GenericAttributeClass fromInt(int i) {
        for (GenericAttributeClass c : GenericAttributeClass.values()) {
            if (c.ordinal() == i)
                return c;
        }

        return UNDEFINED;
    }

    public Class<?> getModelClass() {
        return modelClass;
    }
}
