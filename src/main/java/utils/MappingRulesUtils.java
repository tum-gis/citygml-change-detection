package utils;

import components.Project;
import conf.Rules;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GML;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.xal.XAL;
import org.citygml4j.model.xal.XALClass;
import org.json.JSONObject;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class MappingRulesUtils {
    private static final String[] excluded = {
            "associableClass",
            "cityGMLClass",
            "gMLClass",
            "module",
            "object",
            "parent"
    };

    // Export all accessible attribute names and methods to JSON
    public static <T> void classesToJson(String citygmlJson, String gmlJson, String xalJson,
                                         String printableJson, String genericAttributeJson)
            throws IntrospectionException, NoSuchFieldException {
        write(citygmlJson(), citygmlJson);
        write(gmlJson(), gmlJson);
        write(xalJson(), xalJson);
        write(printableJson(), printableJson);
        write(genericAttributeJson(), genericAttributeJson);
    }

    private static void write(JSONObject json, String filename) {
        try {
            FileWriter file = new FileWriter(filename);
            file.write(json.toString(4));
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONObject citygmlJson() throws IntrospectionException {
        JSONObject result = new JSONObject();
        for (CityGMLClass ele : CityGMLClass.values()) {
            Class<? extends CityGML> modelClass = ele.getModelClass();
            fill(modelClass, result);
        }
        return result;
    }

    private static JSONObject gmlJson() throws IntrospectionException {
        JSONObject result = new JSONObject();
        for (GMLClass ele : GMLClass.values()) {
            Class<? extends GML> modelClass = ele.getModelClass();
            fill(modelClass, result);
        }
        return result;
    }

    private static JSONObject xalJson() throws IntrospectionException {
        JSONObject result = new JSONObject();
        for (XALClass ele : XALClass.values()) {
            Class<? extends XAL> modelClass = ele.getModelClass();
            fill(modelClass, result);
        }
        return result;
    }

    private static JSONObject printableJson() throws IntrospectionException {
        JSONObject result = new JSONObject();
        for (PrintableClass ele : PrintableClass.values()) {
            Class<?> modelClass = ele.getModelClass();
            fillPrintable(modelClass, result);
        }
        return result;
    }

    private static JSONObject genericAttributeJson() throws IntrospectionException, NoSuchFieldException {
        JSONObject result = new JSONObject();
        for (GenericAttributeClass ele : GenericAttributeClass.values()) {
            Class<?> modelClass = ele.getModelClass();
            fillGenericAttribute(modelClass, result);
        }
        return result;
    }

    private static void fill(Class<?> modelClass, JSONObject jsonObject) {
        if (modelClass != null) {
            JSONObject tmp = new JSONObject();
            for (PropertyDescriptor propertyDescriptor
                    : Introspector.getBeanInfo(modelClass).getPropertyDescriptors()) {
                Method getter = propertyDescriptor.getReadMethod();
                // Only consider classes where attributes are directly declared
                if (getter != null && getter.getDeclaringClass().equals(modelClass)) {
                    if (!Project.conf.getMapper().getIsSet() && getter.getName().startsWith("isSet")) {
                        continue;
                    }
                    Class<?> type = getter.getReturnType();
                    tmp.put(getAttributeName(getter.getName()), type.getName());
                }
            }
            for (String exclude : excluded) {
                if (tmp.has(exclude)) {
                    tmp.remove(exclude);
                }
            }
            jsonObject.put(Project.conf.getMapper().getFullClassName() ? modelClass.getName()
                    : modelClass.getSimpleName(), tmp);
        }
    }

    private static void fillPrintable(Class<?> modelClass, JSONObject jsonObject) {
        if (modelClass != null) {
            JSONObject tmp = new JSONObject();
            tmp.put(PropertyNameFactory.value.toString(), modelClass.getName());
            jsonObject.put(Project.conf.getMapper().getFullClassName() ? modelClass.getName()
                    : modelClass.getSimpleName(), tmp);
        }
    }

    private static void fillGenericAttribute(Class<?> modelClass, JSONObject jsonObject) throws NoSuchFieldException {
        if (modelClass != null) {
            JSONObject tmp = new JSONObject();
            tmp.put(PropertyNameFactory.name.toString(), String.class);
            tmp.put(PropertyNameFactory.value.toString(),
                    modelClass.getDeclaredField(PropertyNameFactory.value.toString()).getType());
            jsonObject.put(Project.conf.getMapper().getFullClassName() ? modelClass.getName()
                    : modelClass.getSimpleName(), tmp);
        }
    }

    // Return a single JSON object merged from all JSON files
    public static JSONObject read(String... jsonFiles) throws IOException {
        JSONObject merged = new JSONObject();
        for (String jsonFile : jsonFiles) {
            String content = new String(Files.readAllBytes(Paths.get(jsonFile)));
            JSONObject tmp = new JSONObject(content);
            for (String key : JSONObject.getNames(tmp)) {
                merged.put(key, tmp.get(key));
            }
        }
        return merged;
    }

    public static void main(String[] args) throws IntrospectionException, IOException, NoSuchFieldException {
        Project.init("conf.json", "conf_info.json");
        Rules rules = Project.conf.getMapper().getRules();
        classesToJson(rules.getCitygml(), rules.getGml(), rules.getXal(),
                rules.getPrintable(), rules.getGeneric());
    }
}
