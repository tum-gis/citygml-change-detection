package stats;

import logger.LogUtil;
import matcher.Matcher;

import java.util.HashMap;
import java.util.logging.Logger;

public abstract class Change {
    protected HashMap<String, HashMap<Matcher.EditOperators, Long>> map;

    public Change() {
        this.map = new HashMap<>();
    }

    public boolean contains(String key, Matcher.EditOperators editOperator) {
        HashMap<Matcher.EditOperators, Long> value = this.map.get(key);

        if (value == null) {
            return false;
        }

        value.put(editOperator, value.get(editOperator) + 1);
        return true;
    }

    public void initMapEntry(String key) {
        HashMap<Matcher.EditOperators, Long> value = new HashMap<>();
        value.put(Matcher.EditOperators.INSERT_PROPERTY, new Long(0));
        value.put(Matcher.EditOperators.DELETE_PROPERTY, new Long(0));
        value.put(Matcher.EditOperators.UPDATE_PROPERTY, new Long(0));
        value.put(Matcher.EditOperators.INSERT_NODE, new Long(0));
        value.put(Matcher.EditOperators.DELETE_NODE, new Long(0));
        this.map.put(key, value);
    }

    public void logMap(Logger logger) {
        LogUtil.logMapWithMapValues(logger, this.map, this.getLabel());
    }

    public abstract String getLabel();

    public HashMap<String, HashMap<Matcher.EditOperators, Long>> getMap() {
        return this.map;
    }
}
