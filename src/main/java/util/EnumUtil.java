package util;

import java.util.Arrays;

import org.neo4j.graphdb.Node;

public class EnumUtil {
	// https://stackoverflow.com/questions/13783295/getting-all-names-in-an-enum-as-a-string#answer-13783744
	public static String[] enumValuesToStrings(Class<? extends Enum<?>> e) {
		return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
	}

	/**
	 * Return a header CSV of all enum values.
	 * Example: enum {a,b,c} --> "a;b;c" with ";" as delimiter.
	 * 
	 * @param clss
	 * @param delimiter
	 * @return
	 */
	public static String enumValuesToHeaders(Class<? extends Enum<?>> clss, String delimiter) {
		String[] strings = enumValuesToStrings(clss);

		if (strings.length == 0) {
			return "";
		}

		String result = strings[0];

		if (strings.length == 1) {
			return result;
		}

		for (int i = 1; i < strings.length; i++) {
			result += delimiter + strings[i];
		}

		return result;
	}
	
	public static String getNodePropertiesWithEnums(Node node, Enum<?>[] propertyNames, String delimiter) {
		String result = "";
		
		for (int i = 0; i < propertyNames.length; i++) {
			result += node.getProperty(propertyNames[i].toString()).toString();
			if (i != propertyNames.length - 1) {
				result += delimiter;
			}
		}
		
		return result;
	}
}
