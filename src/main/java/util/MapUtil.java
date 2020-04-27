package util;

import matcher.Matcher;

import java.util.*;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 */
// Inspired by http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
public class MapUtil {
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, (o1, o2) -> {
            // also consider the case where both values are equal -> sort keys
            return (o1.getValue().equals(o2.getValue()) ? o1.getKey().toString().compareTo(o2.getKey().toString())
                    : (o2.getValue()).compareTo(o1.getValue())); // greater values first
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static Map<String, HashMap<Matcher.EditOperators, Long>> sortByHashMapValue(Map<String, HashMap<Matcher.EditOperators, Long>> map) {
        List<Map.Entry<String, HashMap<Matcher.EditOperators, Long>>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> {
            // sum of all values of the hash map value
            Long sum1 = new Long(0);
            Iterator it1 = o1.getValue().entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry pair = (Map.Entry) it1.next();
                sum1 += (Long) pair.getValue();
            }

            // sum of all values of the hash map value
            Long sum2 = new Long(0);
            Iterator it2 = o2.getValue().entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry) it2.next();
                sum2 += (Long) pair.getValue();
            }

            // also consider the case where both values are equal -> sort keys
            return sum1.equals(sum2) ? o1.getKey().toString().compareTo(o2.getKey().toString())
					: Long.compare(sum2, sum1); // greater values first
        });

        // sort both the HashMap values of each entry and the entire HashMap
        Map<String, HashMap<Matcher.EditOperators, Long>> result = new LinkedHashMap<String, HashMap<Matcher.EditOperators, Long>>();
        for (Map.Entry<String, HashMap<Matcher.EditOperators, Long>> entry : list) {
            HashMap<Matcher.EditOperators, Long> newValue = (HashMap<Matcher.EditOperators, Long>) sortByValue(entry.getValue());
            result.put(entry.getKey(), newValue);
        }
        return result;
    }
}
