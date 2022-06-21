package components.mapper;

import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.common.base.ModelClassEnum;
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

public class ReflectionUtils {

    // Export all accessible attribute names and methods to JSON
    public static <T> void classesToJson(String citygmlJson, String gmlJson, String xalJson)
            throws IntrospectionException {
        write(citygmlJson(), citygmlJson);
        write(gmlJson(), gmlJson);
        write(xalJson(), xalJson);
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
            if (modelClass != null) {
                JSONObject tmp = fillJson((Class<? extends ModelClassEnum>) modelClass);
                result.put(modelClass.getName(), tmp);
            }
        }
        return result;
    }

    private static JSONObject gmlJson() throws IntrospectionException {
        JSONObject result = new JSONObject();
        for (GMLClass ele : GMLClass.values()) {
            Class<? extends GML> modelClass = ele.getModelClass();
            if (modelClass != null) {
                JSONObject tmp = fillJson((Class<? extends ModelClassEnum>) modelClass);
                result.put(modelClass.getName(), tmp);
            }
        }
        return result;
    }

    private static JSONObject xalJson() throws IntrospectionException {
        JSONObject result = new JSONObject();
        for (XALClass ele : XALClass.values()) {
            Class<? extends XAL> modelClass = ele.getModelClass();
            if (modelClass != null) {
                JSONObject tmp = fillJson((Class<? extends ModelClassEnum>) modelClass);
                result.put(modelClass.getName(), tmp);
            }
        }
        return result;
    }

    private static JSONObject fillJson(Class<? extends ModelClassEnum> modelClass) throws IntrospectionException {
        JSONObject result = new JSONObject();
        for (PropertyDescriptor propertyDescriptor
                : Introspector.getBeanInfo(modelClass).getPropertyDescriptors()) {
            Method getter = propertyDescriptor.getReadMethod();
            // Only consider classes where attributes are directly declared
            if (getter != null && getter.getDeclaringClass().equals(modelClass)) {
                Class<?> type = getter.getReturnType();
                result.put(getter.getName(), type.getName());
            }
        }
        return result;
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

    public static void main(String[] args) throws IntrospectionException {
        String base = "config/mapper";
        classesToJson(base + "citygml.json", base + "gml.json", base + "xal.json");
    }
}
