package stats;

import logger.LogUtil;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import java.util.logging.Logger;

public class OtherChange extends Change {

    public OtherChange() {
        super();
    }

    @Override
    // here every property / node type shall be added to the list
    public boolean contains(String propertyName, String ofNodeType) {
        // propertyName first, then ofNodeType
        if (propertyName == null || propertyName.isEmpty()) {
            Long value = this.map.get(ofNodeType);
            if (value == null) {
                this.map.put(ofNodeType, new Long(0));
                return false;
            }
            this.map.put(ofNodeType, value + 1);
            return true;
        } else {
            Long value = this.map.get(propertyName);
            if (value == null) {
                // if property name is not contained in the list, search for node type
                value = this.map.get(ofNodeType);
                if (value == null) {
                    this.map.put(ofNodeType, new Long(0));
                    return false;
                }
                this.map.put(ofNodeType, value + 1);
                return true;
            }

            this.map.put(propertyName, value + 1);
            return true;
        }
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Other Changes");
    }
}
