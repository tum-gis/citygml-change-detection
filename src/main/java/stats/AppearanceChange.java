package stats;

import logger.LogUtil;
import mapper.EnumClasses;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import java.util.HashMap;
import java.util.logging.Logger;

public class AppearanceChange extends Change {

    public AppearanceChange() {
        super();
        // TODO find the rest
        this.map.put("imageURI", new Long(0));
        this.map.put("theme", new Long(0));
        this.map.put("shininess", new Long(0));
        this.map.put(GMLClass.RING.toString(), new Long(0));
    }

    @Override
    public void printMap(Logger logger) {
        LogUtil.logMap(logger, this.map, "Changes in Appearance");
    }
}
