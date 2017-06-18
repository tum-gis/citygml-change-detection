package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
// Inspired by http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
public class MapUtil {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// also consider the case where both values are equal -> sort keys
				return (o1.getValue().equals(o2.getValue()) ? o1.getKey().toString().compareTo(o2.getKey().toString())
						: (o2.getValue()).compareTo(o1.getValue())); // greater values first
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
