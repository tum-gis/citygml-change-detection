package stats;

import logger.LogUtil;

import java.util.HashMap;
import java.util.logging.Logger;

public abstract class Change {
    protected HashMap<String, Long> map;

    public Change() {
        this.map = new HashMap<>();
    }

    public boolean contains(String propertyName, String ofNodeType) {
        // if propertyName is invalid --> compare ofNodeType instead
        if (propertyName == null || propertyName.isEmpty()) {
            Long value = this.map.get(ofNodeType);
            if (value == null) {
                return false;
            }
            this.map.put(ofNodeType, value + 1);
            return true;
        } else {
            // first compare property name
            Long value = this.map.get(propertyName);
            if (value == null) {
                // if property name is not contained in the list, search for node type
                value = this.map.get(ofNodeType);
                if (value == null) {
                    return false;
                }
                this.map.put(ofNodeType, value + 1);
                return true;
            }

            this.map.put(propertyName, value + 1);
            return true;
        }
    }

    public abstract void printMap(Logger logger);
}
